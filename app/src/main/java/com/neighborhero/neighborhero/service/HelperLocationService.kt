package com.neighborhero.neighborhero.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.neighborhero.neighborhero.MainActivity
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.dto.LiveTrackingDto
import com.neighborhero.neighborhero.data.network.dto.UpdateLiveLocationDto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HelperLocationService : Service(), LocationListener {

    companion object {
        const val ACTION_START = "com.neighborhero.action.START_LOCATION_TRACKING"
        const val ACTION_STOP = "com.neighborhero.action.STOP_LOCATION_TRACKING"
        const val EXTRA_TASK_ID = "extra_task_id"
        private const val NOTIFICATION_ID = 4040
        private const val CHANNEL_ID = "neighborhero_task_tracking"

        // Observable state for UI components
        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _currentLocation = MutableStateFlow<Location?>(null)
        val currentLocation: StateFlow<Location?> = _currentLocation.asStateFlow()

        private val _latestTracking = MutableStateFlow<LiveTrackingDto?>(null)
        val latestTracking: StateFlow<LiveTrackingDto?> = _latestTracking.asStateFlow()

        private val _activeTaskId = MutableStateFlow<String?>(null)
        val activeTaskId: StateFlow<String?> = _activeTaskId.asStateFlow()

        fun startTracking(context: Context, taskId: String) {
            val intent = Intent(context, HelperLocationService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_TASK_ID, taskId)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopTracking(context: Context) {
            val intent = Intent(context, HelperLocationService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var locationManager: LocationManager? = null
    private var currentTaskId: String? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val taskId = intent.getStringExtra(EXTRA_TASK_ID)
                if (!taskId.isNullOrBlank()) {
                    currentTaskId = taskId
                    _activeTaskId.value = taskId
                    startForegroundTracking(taskId)
                }
            }
            ACTION_STOP -> {
                stopTrackingInternal()
            }
        }
        return START_NOT_STICKY
    }

    private fun startForegroundTracking(taskId: String) {
        val notification = buildTrackingNotification("Sharing live GPS location with requester")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }

        _isRunning.value = true
        registerLocationListener()
    }

    @SuppressLint("MissingPermission")
    private fun registerLocationListener() {
        val lm = locationManager ?: return

        val hasFine = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            stopTrackingInternal()
            return
        }

        try {
            // Check last known location first for immediate initial position
            val lastGps = if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } else null
            val lastNetwork = if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } else null

            val bestLast = when {
                lastGps != null && lastNetwork != null -> if (lastGps.time >= lastNetwork.time) lastGps else lastNetwork
                lastGps != null -> lastGps
                else -> lastNetwork
            }

            if (bestLast != null) {
                onLocationChanged(bestLast)
            }

            // Register continuous updates: 3000ms (3s), 1 meter distance threshold
            if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    3000L,
                    1.0f,
                    this
                )
            }
            if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                lm.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    4000L,
                    2.0f,
                    this
                )
            }
        } catch (e: Exception) {
            android.util.Log.e("HelperLocationService", "Failed to register location updates: ${e.message}")
        }
    }

    override fun onLocationChanged(location: Location) {
        _currentLocation.value = location
        val taskId = currentTaskId ?: return

        serviceScope.launch {
            try {
                val dto = UpdateLiveLocationDto(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = if (location.hasAccuracy()) location.accuracy.toDouble() else 5.0,
                    speed = if (location.hasSpeed()) location.speed.toDouble() else null,
                    heading = if (location.hasBearing()) location.bearing.toDouble() else null
                )

                val response = NetworkClient.getService().updateLiveLocation(taskId, dto)
                if (response.isSuccessful && response.body()?.success == true) {
                    val tracking = response.body()!!.data
                    _latestTracking.value = tracking

                    // Update notification with distance/ETA if available
                    val etaStr = if (tracking?.etaMinutes != null && tracking.etaMinutes > 0) {
                        " • ETA ~${tracking.etaMinutes} min"
                    } else ""
                    val distStr = if (tracking?.distanceMiles != null) {
                        " • ${tracking.distanceMiles} mi away"
                    } else ""
                    val statusText = "En route to requester$distStr$etaStr"
                    updateNotification(statusText)
                } else if (response.code() == 400 || response.code() == 403 || response.code() == 404) {
                    // Task finished or no longer IN_PROGRESS
                    stopTrackingInternal()
                }
            } catch (e: Exception) {
                android.util.Log.e("HelperLocationService", "Failed to push live location: ${e.message}")
            }
        }
    }

    private fun updateNotification(content: String) {
        try {
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.notify(NOTIFICATION_ID, buildTrackingNotification(content))
        } catch (_: Exception) {}
    }

    private fun buildTrackingNotification(content: String): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = Intent(this, HelperLocationService::class.java).apply {
            action = ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("NeighborHero — Helper Active Tracking")
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_notification)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop Sharing",
                stopPendingIntent
            )
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Task Live Tracking",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows live location sharing status while assisting a neighbor"
                setShowBadge(false)
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            nm?.createNotificationChannel(channel)
        }
    }

    private fun stopTrackingInternal() {
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Exception) {}

        val taskId = currentTaskId
        if (!taskId.isNullOrBlank()) {
            serviceScope.launch {
                try {
                    NetworkClient.getService().stopLiveLocation(taskId)
                } catch (_: Exception) {}
            }
        }

        _isRunning.value = false
        _currentLocation.value = null
        _latestTracking.value = null
        _activeTaskId.value = null
        currentTaskId = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onProviderEnabled(provider: String) {}
    override fun onProviderDisabled(provider: String) {}
    @Deprecated("Deprecated in Java")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationManager?.removeUpdates(this)
        } catch (_: Exception) {}
        serviceScope.cancel()
        _isRunning.value = false
    }
}
