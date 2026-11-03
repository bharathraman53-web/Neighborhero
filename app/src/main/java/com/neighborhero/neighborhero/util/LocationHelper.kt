package com.neighborhero.neighborhero.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale
import kotlin.coroutines.resume

data class UserLocationResult(
    val latitude: Double,
    val longitude: Double,
    val addressSummary: String,
    val isPrecise: Boolean
)

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    fun isFineLocationGranted(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun isLocationServiceEnabled(context: Context): Boolean {
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return false
        val gpsEnabled = try {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (_: Exception) {
            false
        }
        val networkEnabled = try {
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (_: Exception) {
            false
        }
        return gpsEnabled || networkEnabled
    }

    fun openAppSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    fun openLocationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {}
    }

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Result<UserLocationResult> {
        if (!hasLocationPermission(context)) {
            return Result.failure(SecurityException("NeighborHero uses your location to find suitable nearby helpers."))
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return Result.failure(IllegalStateException("Location services are unavailable on this device."))

        if (!isLocationServiceEnabled(context)) {
            return Result.failure(IllegalStateException("Location services are turned off. Please enable GPS."))
        }

        val isPrecise = isFineLocationGranted(context)

        // 1. Try to find recent cached location (< 90 seconds old)
        var bestLocation: Location? = null
        val providers = listOf(
            LocationManager.GPS_PROVIDER,
            LocationManager.NETWORK_PROVIDER,
            LocationManager.PASSIVE_PROVIDER
        )

        for (provider in providers) {
            try {
                if (lm.isProviderEnabled(provider)) {
                    val loc = lm.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.time > bestLocation.time) {
                            bestLocation = loc
                        }
                    }
                }
            } catch (_: Exception) {}
        }

        val now = System.currentTimeMillis()
        if (bestLocation != null && (now - bestLocation.time) < 90_000L) {
            val summary = reverseGeocode(context, bestLocation.latitude, bestLocation.longitude)
            return Result.success(
                UserLocationResult(
                    latitude = bestLocation.latitude,
                    longitude = bestLocation.longitude,
                    addressSummary = summary,
                    isPrecise = isPrecise
                )
            )
        }

        // 2. Request a fresh single-shot location update with a 6-second timeout
        val freshLocation = withTimeoutOrNull(6000L) {
            requestSingleLocationUpdate(lm)
        } ?: bestLocation // fallback to older lastKnownLocation if available

        if (freshLocation == null) {
            return Result.failure(IllegalStateException("Unable to acquire location coordinates. Please try again or type your neighborhood manually."))
        }

        val summary = reverseGeocode(context, freshLocation.latitude, freshLocation.longitude)
        return Result.success(
            UserLocationResult(
                latitude = freshLocation.latitude,
                longitude = freshLocation.longitude,
                addressSummary = summary,
                isPrecise = isPrecise
            )
        )
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleLocationUpdate(lm: LocationManager): Location? =
        suspendCancellableCoroutine { continuation ->
            val provider = when {
                lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> LocationManager.PASSIVE_PROVIDER
            }

            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    lm.removeUpdates(this)
                    if (continuation.isActive) {
                        continuation.resume(location)
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            try {
                lm.requestLocationUpdates(
                    provider,
                    0L,
                    0f,
                    listener,
                    Looper.getMainLooper()
                )
            } catch (e: Exception) {
                if (continuation.isActive) {
                    continuation.resume(null)
                }
                return@suspendCancellableCoroutine
            }

            continuation.invokeOnCancellation {
                Handler(Looper.getMainLooper()).post {
                    try {
                        lm.removeUpdates(listener)
                    } catch (_: Exception) {}
                }
            }
        }

    private suspend fun reverseGeocode(context: Context, lat: Double, lng: Double): String =
        withContext(Dispatchers.IO) {
            try {
                if (!Geocoder.isPresent()) {
                    return@withContext String.format(Locale.US, "%.4f, %.4f", lat, lng)
                }
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    val neighborhood = addr.subLocality ?: addr.locality ?: addr.subAdminArea ?: addr.adminArea
                    val thoroughfare = addr.thoroughfare
                    val city = addr.locality
                    when {
                        !neighborhood.isNullOrBlank() && !city.isNullOrBlank() && neighborhood != city ->
                            "$neighborhood, $city"
                        !neighborhood.isNullOrBlank() ->
                            neighborhood
                        !thoroughfare.isNullOrBlank() && !city.isNullOrBlank() ->
                            "$thoroughfare, $city"
                        !thoroughfare.isNullOrBlank() ->
                            thoroughfare
                        !addr.getAddressLine(0).isNullOrBlank() ->
                            addr.getAddressLine(0).split(",").take(2).joinToString(", ").trim()
                        else ->
                            String.format(Locale.US, "%.4f, %.4f", lat, lng)
                    }
                } else {
                    String.format(Locale.US, "%.4f, %.4f", lat, lng)
                }
            } catch (_: Exception) {
                String.format(Locale.US, "%.4f, %.4f", lat, lng)
            }
        }
}
