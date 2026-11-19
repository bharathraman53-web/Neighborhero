package com.neighborhero.neighborhero.feature.task

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.MotionEvent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.network.RoadRouteResult
import com.neighborhero.neighborhero.data.network.RoadRoutingService
import com.neighborhero.neighborhero.data.network.dto.LiveTrackingDto
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.LocalIsDarkTheme
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import org.osmdroid.views.overlay.TilesOverlay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrackHelperScreen(
    task: HelpRequest?,
    tracking: LiveTrackingDto?,
    onBackClick: () -> Unit,
    onChatClick: () -> Unit,
    onRefresh: () -> Unit,
    roadRoute: RoadRouteResult? = null
) {
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val helperName = tracking?.helperName ?: task?.assignedHelperName ?: "Assigned Helper"
    val destination = task?.location?.ifBlank { "Requester Location" } ?: "Requester Location"
    val isTrackingActive = tracking != null && tracking.isActive && tracking.latitude != null && tracking.longitude != null

    // Destination GPS (from task or live tracking dto, with safe fallback to neighborhood centroid)
    val destLat = task?.latitude?.takeIf { it != 0.0 }
        ?: tracking?.requesterLatitude?.takeIf { it != 0.0 }
        ?: 13.0827
    val destLng = task?.longitude?.takeIf { it != 0.0 }
        ?: tracking?.requesterLongitude?.takeIf { it != 0.0 }
        ?: 80.2707

    // Helper GPS
    val helperLat = tracking?.latitude
    val helperLng = tracking?.longitude

    // Road-following route state (uses ViewModel roadRoute, with local service fallback)
    var localRoadRoute by remember { mutableStateOf<RoadRouteResult?>(null) }
    var isRouteLoading by remember { mutableStateOf(false) }
    val effectiveRoute = roadRoute ?: localRoadRoute

    // Fetch road route fallback if not supplied by ViewModel
    LaunchedEffect(helperLat, helperLng, destLat, destLng, isTrackingActive) {
        if (isTrackingActive && helperLat != null && helperLng != null) {
            val origin = GeoPoint(helperLat, helperLng)
            val dest = GeoPoint(destLat, destLng)
            if (localRoadRoute == null || RoadRoutingService.getInstance().shouldRecalculate(origin, dest)) {
                isRouteLoading = true
                val result = RoadRoutingService.getInstance().fetchRoadRoute(helperLat, helperLng, destLat, destLng)
                if (result.isSuccess) {
                    localRoadRoute = result.getOrNull()
                }
                isRouteLoading = false
            }
        }
    }

    // MapView reference for interactive zoom controls & re-center
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var initialCameraFramed by remember { mutableStateOf(false) }

    // Markers and overlay references
    var requesterMarker by remember { mutableStateOf<Marker?>(null) }
    var helperMarker by remember { mutableStateOf<Marker?>(null) }
    var routePolyline by remember { mutableStateOf<Polyline?>(null) }
    var isMapTilesLoaded by remember { mutableStateOf(false) }

    // Approximate distance in kilometers between helper and requester using Haversine formula
    val approxDistanceKm = remember(helperLat, helperLng, destLat, destLng) {
        if (helperLat != null && helperLng != null && helperLat != 0.0 && helperLng != 0.0 && destLat != 0.0 && destLng != 0.0) {
            val dMeters = RoadRoutingService.distanceBetweenMeters(helperLat, helperLng, destLat, destLng)
            dMeters / 1000.0
        } else null
    }

    // Dynamic remaining road route:
    // 1. Starts from helper's CURRENT GPS position.
    // 2. Removes the already-travelled portion of the route behind the helper.
    // 3. Follows actual roads to the requester's destination.
    // 4. Never draws a straight line.
    val remainingRoutePoints = remember(effectiveRoute, helperLat, helperLng) {
        if (effectiveRoute != null && effectiveRoute.points.isNotEmpty() && helperLat != null && helperLng != null) {
            val helperGeo = GeoPoint(helperLat, helperLng)
            RoadRoutingService.trimRemainingRoadRoute(helperGeo, effectiveRoute.points)
        } else {
            emptyList()
        }
    }

    // Last updated timestamp display
    val lastUpdatedDisplay = remember(tracking?.updatedAt) {
        if (!tracking?.updatedAt.isNullOrBlank()) {
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val outputFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
                val parsed = inputFormat.parse(tracking!!.updatedAt!!)
                if (parsed != null) "Updated ${outputFormat.format(parsed)}" else "Updated recently"
            } catch (_: Exception) {
                "Live updates active"
            }
        } else {
            val nowFormat = SimpleDateFormat("h:mm:ss a", Locale.getDefault())
            "Updated ${nowFormat.format(Date())}"
        }
    }

    // Attach lifecycle observer for MapView to prevent memory leaks and resume tile loading.
    // Key ONLY on lifecycleOwner so onDispose is NEVER triggered prematurely during composition.
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                Lifecycle.Event.ON_DESTROY -> {
                    mapViewRef?.onDetach()
                    mapViewRef = null
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onPause()
            mapViewRef?.onDetach()
            mapViewRef = null
        }
    }

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Live Helper Tracking",
                    onBackClick = onBackClick
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // 1. Helper Live Status Card
                GlassFocusCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.accent.copy(alpha = 0.15f))
                                    .border(1.5.dp, GlassTheme.cardBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!tracking?.helperAvatar.isNullOrBlank()) {
                                    NetworkImage(
                                        url = tracking?.helperAvatar,
                                        modifier = Modifier.fillMaxSize(),
                                        contentDescription = helperName
                                    ) {
                                        Text(
                                            text = helperName.take(1).uppercase(),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GlassTheme.accent
                                        )
                                    }
                                } else {
                                    Text(
                                        text = helperName.take(1).uppercase(),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GlassTheme.accent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = helperName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(if (isTrackingActive) Color(0xFF10B981) else Color(0xFFF59E0B))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isTrackingActive) "Helper is on the way" else "Waiting for helper to start",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (isTrackingActive) Color(0xFF10B981) else Color(0xFFF59E0B)
                                    )
                                }
                                if (isTrackingActive && approxDistanceKm != null) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    val distLabel = if (approxDistanceKm < 0.1) {
                                        "Arriving now (< 100 m away)"
                                    } else {
                                        String.format(Locale.US, "Approx. %.1f km away", approxDistanceKm)
                                    }
                                    Text(
                                        text = distLabel,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = GlassTheme.textSecondary
                                    )
                                }
                            }
                        }

                        // Call & Chat Shortcuts
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable { onChatClick() },
                                shape = CircleShape,
                                color = GlassTheme.chipBackground,
                                border = BorderStroke(1.dp, GlassTheme.chipBorder)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Chat,
                                        contentDescription = "Chat",
                                        tint = GlassTheme.textPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            val phoneToDial = task?.requesterPhone?.takeIf { it.isNotBlank() } ?: "5551234567"
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToDial"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    },
                                shape = CircleShape,
                                color = GlassTheme.chipBackground,
                                border = BorderStroke(1.dp, GlassTheme.chipBorder)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = GlassTheme.accent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // 2. Real Interactive Map Container (OpenStreetMap via osmdroid)
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    shape = RoundedCornerShape(26.dp),
                    alpha = 0.95f
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(26.dp))
                    ) {
                        // Embedded Real OpenStreetMap View
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    setTileSource(TileSourceFactory.MAPNIK)
                                    setMultiTouchControls(true)
                                    zoomController.setVisibility(org.osmdroid.views.CustomZoomButtonsController.Visibility.NEVER)

                                    // CRITICAL: Eliminate map blurriness and pixelation.
                                    // 1. Disable tile scaling to screen DPI: standard 256x256 OSM tiles were previously
                                    //    being upscaled by 200%-300% (e.g. 768px on 480dpi screens), creating extreme fuzziness.
                                    //    Setting isTilesScaledToDpi = false renders tiles at native 1:1 pixel sharpness.
                                    isTilesScaledToDpi = false
                                    setTilesScaleFactor(1.0f)
                                    minZoomLevel = 4.0
                                    maxZoomLevel = 20.0

                                    // Intercept touch so vertical parent scroll doesn't interfere with map gestures
                                    setOnTouchListener { v, event ->
                                        when (event.action) {
                                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                                                v.parent?.requestDisallowInterceptTouchEvent(true)
                                            }
                                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                                                v.parent?.requestDisallowInterceptTouchEvent(false)
                                            }
                                        }
                                        false
                                    }

                                    // Optimize tile overlay rendering and seamless background loading
                                    overlayManager.tilesOverlay.apply {
                                        val loadingBg = if (isDark) 0xFF0F172A.toInt() else 0xFFF8FAFC.toInt()
                                        setLoadingBackgroundColor(loadingBg)
                                        setLoadingLineColor(android.graphics.Color.TRANSPARENT)
                                        if (isDark) {
                                            setColorFilter(TilesOverlay.INVERT_COLORS)
                                        } else {
                                            setColorFilter(null)
                                        }

                                        // Register tile loading completion listener
                                        tileStates.runAfters.add(Runnable {
                                            val states = tileStates
                                            if (states != null && states.isDone) {
                                                if (states.total == 0 || states.upToDate + states.notFound >= states.total) {
                                                    if (!isMapTilesLoaded) {
                                                        post { isMapTilesLoaded = true }
                                                    }
                                                }
                                            }
                                        })
                                    }

                                    // Create Requester Marker
                                    val reqPin = Marker(this).apply {
                                        position = GeoPoint(destLat, destLng)
                                        title = "Requester: $destination"
                                        snippet = "Task Destination"
                                        val (reqDrawable, anchorX, anchorY) = createRequesterMarkerDrawable(ctx, destination)
                                        icon = reqDrawable
                                        setAnchor(anchorX, anchorY)
                                        infoWindow = null
                                    }
                                    overlays.add(reqPin)
                                    requesterMarker = reqPin

                                    // Create Helper Marker if active
                                    if (isTrackingActive && helperLat != null && helperLng != null) {
                                        val hLat = helperLat
                                        val hLng = helperLng
                                        val helpPin = Marker(this).apply {
                                            position = GeoPoint(hLat, hLng)
                                            title = "$helperName (Helper)"
                                            snippet = "Live Location • En Route"
                                            val (hDrawable, anchorX, anchorY) = createHelperMarkerDrawable(ctx, helperName)
                                            icon = hDrawable
                                            setAnchor(anchorX, anchorY)
                                            infoWindow = null
                                        }
                                        overlays.add(helpPin)
                                        helperMarker = helpPin

                                        // Polyline following actual roads (OSRM).
                                        // Starts from helper's CURRENT GPS position and leads to destination.
                                        // Removes already-travelled portions. NEVER fallback to straight line: if road route is still loading,
                                        // only the markers are displayed until geometry is ready.
                                        if (remainingRoutePoints.isNotEmpty()) {
                                            val poly = Polyline(this).apply {
                                                outlinePaint.color = android.graphics.Color.parseColor("#0284C7")
                                                outlinePaint.strokeWidth = 6f * ctx.resources.displayMetrics.density
                                                outlinePaint.strokeCap = Paint.Cap.ROUND
                                                outlinePaint.strokeJoin = Paint.Join.ROUND
                                                setPoints(remainingRoutePoints)
                                            }
                                            overlays.add(poly)
                                            routePolyline = poly
                                        }
                                    }

                                    // Auto-frame initial camera when layout is computed
                                    addOnFirstLayoutListener { _, _, _, _, _ ->
                                        try {
                                            val displayPts = remainingRoutePoints.ifEmpty { effectiveRoute?.points ?: emptyList() }
                                            if (displayPts.isNotEmpty()) {
                                                val lats = displayPts.map { it.latitude }
                                                val lngs = displayPts.map { it.longitude }
                                                val minLat = lats.minOrNull() ?: destLat
                                                val maxLat = lats.maxOrNull() ?: destLat
                                                val minLng = lngs.minOrNull() ?: destLng
                                                val maxLng = lngs.maxOrNull() ?: destLng
                                                val latPad = maxOf((maxLat - minLat) * 0.25, 0.003)
                                                val lngPad = maxOf((maxLng - minLng) * 0.25, 0.003)
                                                val bbox = BoundingBox(maxLat + latPad, maxLng + lngPad, minLat - latPad, minLng - lngPad)
                                                zoomToBoundingBox(bbox, false, 140)
                                            } else if (isTrackingActive && helperLat != null && helperLng != null) {
                                                val hLat = helperLat
                                                val hLng = helperLng
                                                val minLat = minOf(destLat, hLat)
                                                val maxLat = maxOf(destLat, hLat)
                                                val minLng = minOf(destLng, hLng)
                                                val maxLng = maxOf(destLng, hLng)
                                                val latPad = maxOf((maxLat - minLat) * 0.35, 0.004)
                                                val lngPad = maxOf((maxLng - minLng) * 0.35, 0.004)
                                                val bbox = BoundingBox(maxLat + latPad, maxLng + lngPad, minLat - latPad, minLng - lngPad)
                                                zoomToBoundingBox(bbox, false, 140)
                                            } else {
                                                controller.setCenter(GeoPoint(destLat, destLng))
                                                controller.setZoom(17.0)
                                            }
                                            initialCameraFramed = true
                                        } catch (e: Exception) {
                                            android.util.Log.e("TrackHelperScreen", "Error on initial framing: ${e.message}")
                                        }
                                    }

                                    mapViewRef = this
                                }
                            },
                            update = { map ->
                                try {
                                    // Enforce native 1:1 tile resolution (prevent scaling/blurring)
                                    if (map.isTilesScaledToDpi) {
                                        map.isTilesScaledToDpi = false
                                    }
                                    if (map.tilesScaleFactor != 1.0f) {
                                        map.setTilesScaleFactor(1.0f)
                                    }

                                    // Keep tile overlay background and color filter in sync with theme
                                    map.overlayManager.tilesOverlay.apply {
                                        val loadingBg = if (isDark) 0xFF0F172A.toInt() else 0xFFF8FAFC.toInt()
                                        if (loadingBackgroundColor != loadingBg) {
                                            setLoadingBackgroundColor(loadingBg)
                                        }
                                        setLoadingLineColor(android.graphics.Color.TRANSPARENT)
                                        if (isDark) {
                                            setColorFilter(TilesOverlay.INVERT_COLORS)
                                        } else {
                                            setColorFilter(null)
                                        }
                                    }

                                    // Update Requester Marker position
                                    requesterMarker?.position = GeoPoint(destLat, destLng)

                                    // Update Helper Marker & Road Route Polyline as GPS / route coordinates update
                                    if (isTrackingActive && helperLat != null && helperLng != null) {
                                        val hLat = helperLat
                                        val hLng = helperLng
                                        val helperGeo = GeoPoint(hLat, hLng)

                                        val currentHelper = helperMarker
                                        if (currentHelper == null) {
                                            val helpPin = Marker(map).apply {
                                                position = helperGeo
                                                title = "$helperName (Helper)"
                                                snippet = "Live Location • En Route"
                                                val (hDrawable, anchorX, anchorY) = createHelperMarkerDrawable(context, helperName)
                                                icon = hDrawable
                                                setAnchor(anchorX, anchorY)
                                                infoWindow = null
                                            }
                                            map.overlays.add(helpPin)
                                            helperMarker = helpPin
                                        } else {
                                            currentHelper.position = helperGeo
                                            if (!map.overlays.contains(currentHelper)) {
                                                map.overlays.add(currentHelper)
                                            }
                                        }

                                        // Road Route Polyline updating:
                                        // Draws ONLY the remaining road-route geometry starting from helper's latest position.
                                        // Removes already-travelled portions. NEVER draws straight-line fallback.
                                        val currentPoly = routePolyline
                                        if (remainingRoutePoints.isNotEmpty()) {
                                            if (currentPoly == null) {
                                                val poly = Polyline(map).apply {
                                                    outlinePaint.color = android.graphics.Color.parseColor("#0284C7")
                                                    outlinePaint.strokeWidth = 6f * context.resources.displayMetrics.density
                                                    outlinePaint.strokeCap = Paint.Cap.ROUND
                                                    outlinePaint.strokeJoin = Paint.Join.ROUND
                                                    setPoints(remainingRoutePoints)
                                                }
                                                map.overlays.add(poly)
                                                routePolyline = poly
                                            } else {
                                                try {
                                                    currentPoly.setPoints(remainingRoutePoints)
                                                    if (!map.overlays.contains(currentPoly)) {
                                                        map.overlays.add(currentPoly)
                                                    }
                                                } catch (_: Exception) {
                                                    // Re-create polyline defensively if detached
                                                    map.overlays.remove(currentPoly)
                                                    val poly = Polyline(map).apply {
                                                        outlinePaint.color = android.graphics.Color.parseColor("#0284C7")
                                                        outlinePaint.strokeWidth = 6f * context.resources.displayMetrics.density
                                                        outlinePaint.strokeCap = Paint.Cap.ROUND
                                                        outlinePaint.strokeJoin = Paint.Join.ROUND
                                                        setPoints(remainingRoutePoints)
                                                    }
                                                    map.overlays.add(poly)
                                                    routePolyline = poly
                                                }
                                            }
                                        } else {
                                            // No road route available yet: remove polyline, do not draw a straight line
                                            currentPoly?.let { map.overlays.remove(it) }
                                            routePolyline = null
                                        }

                                        // If not yet framed, frame now
                                        if (!initialCameraFramed && map.isLayoutOccurred) {
                                            val displayPts = remainingRoutePoints.ifEmpty { effectiveRoute?.points ?: emptyList() }
                                            if (displayPts.isNotEmpty()) {
                                                val lats = displayPts.map { it.latitude }
                                                val lngs = displayPts.map { it.longitude }
                                                val minLat = lats.minOrNull() ?: destLat
                                                val maxLat = lats.maxOrNull() ?: destLat
                                                val minLng = lngs.minOrNull() ?: destLng
                                                val maxLng = lngs.maxOrNull() ?: destLng
                                                val latPad = maxOf((maxLat - minLat) * 0.25, 0.003)
                                                val lngPad = maxOf((maxLng - minLng) * 0.25, 0.003)
                                                val bbox = BoundingBox(maxLat + latPad, maxLng + lngPad, minLat - latPad, minLng - lngPad)
                                                map.zoomToBoundingBox(bbox, true, 140)
                                                initialCameraFramed = true
                                            } else {
                                                val minLat = minOf(destLat, hLat)
                                                val maxLat = maxOf(destLat, hLat)
                                                val minLng = minOf(destLng, hLng)
                                                val maxLng = maxOf(destLng, hLng)
                                                val latPad = maxOf((maxLat - minLat) * 0.35, 0.004)
                                                val lngPad = maxOf((maxLng - minLng) * 0.35, 0.004)
                                                val bbox = BoundingBox(maxLat + latPad, maxLng + lngPad, minLat - latPad, minLng - lngPad)
                                                map.zoomToBoundingBox(bbox, true, 140)
                                                initialCameraFramed = true
                                            }
                                        }
                                    } else {
                                        // Remove helper marker and route if inactive
                                        helperMarker?.let { map.overlays.remove(it) }
                                        helperMarker = null
                                        routePolyline?.let { map.overlays.remove(it) }
                                        routePolyline = null
                                    }

                                    map.invalidate()
                                } catch (e: Exception) {
                                    android.util.Log.e("TrackHelperScreen", "Error updating map overlays: ${e.message}")
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top-Left Badge: Real OpenStreetMap Attribution Indicator
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = (if (isDark) Color(0xFF0F172A) else Color.White).copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, GlassTheme.cardBorder),
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    tint = GlassTheme.accent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (!isMapTilesLoaded) "OpenStreetMap • Loading..." else "OpenStreetMap • Live",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = GlassTheme.textPrimary
                                )
                            }
                        }

                        // Top-Right Badge: Live GPS Lock Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = (if (isDark) Color(0xFF0F172A) else Color.White).copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, GlassTheme.cardBorder),
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isTrackingActive) Icons.Default.GpsFixed else Icons.Default.GpsNotFixed,
                                    contentDescription = null,
                                    tint = if (isTrackingActive) Color(0xFF10B981) else Color(0xFFF59E0B),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isTrackingActive) "GPS Active" else "Standing By",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                    color = GlassTheme.textPrimary
                                )
                            }
                        }

                        // Bottom-Left Badge: Last GPS Update Timestamp
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = (if (isDark) Color(0xFF0F172A) else Color.White).copy(alpha = 0.88f),
                            border = BorderStroke(1.dp, GlassTheme.cardBorder),
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = GlassTheme.accent,
                                    modifier = Modifier.size(11.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = lastUpdatedDisplay,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Medium),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }

                        // Bottom-Right Column: Real Map Zoom Controls (+ / -) & Re-center
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Zoom In (+)
                            Surface(
                                shape = CircleShape,
                                color = (if (isDark) Color(0xFF0F172A) else Color.White).copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, GlassTheme.cardBorder),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { mapViewRef?.controller?.zoomIn() },
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Add,
                                        contentDescription = "Zoom In",
                                        tint = GlassTheme.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Zoom Out (-)
                            Surface(
                                shape = CircleShape,
                                color = (if (isDark) Color(0xFF0F172A) else Color.White).copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, GlassTheme.cardBorder),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable { mapViewRef?.controller?.zoomOut() },
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Remove,
                                        contentDescription = "Zoom Out",
                                        tint = GlassTheme.textPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            // Re-center / Fit Markers or Road Route
                            Surface(
                                shape = CircleShape,
                                color = (if (isDark) Color(0xFF0F172A) else Color.White).copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, GlassTheme.accent),
                                modifier = Modifier
                                    .size(36.dp)
                                    .clickable {
                                        mapViewRef?.let { map ->
                                            try {
                                                val routePts = remainingRoutePoints.ifEmpty { effectiveRoute?.points ?: emptyList() }
                                                if (routePts.isNotEmpty()) {
                                                    val lats = routePts.map { it.latitude }
                                                    val lngs = routePts.map { it.longitude }
                                                    val minLat = lats.minOrNull() ?: destLat
                                                    val maxLat = lats.maxOrNull() ?: destLat
                                                    val minLng = lngs.minOrNull() ?: destLng
                                                    val maxLng = lngs.maxOrNull() ?: destLng
                                                    val latPad = maxOf((maxLat - minLat) * 0.25, 0.003)
                                                    val lngPad = maxOf((maxLng - minLng) * 0.25, 0.003)
                                                    val bbox = BoundingBox(maxLat + latPad, maxLng + lngPad, minLat - latPad, minLng - lngPad)
                                                    map.zoomToBoundingBox(bbox, true, 140)
                                                } else if (isTrackingActive && helperLat != null && helperLng != null) {
                                                    val hLat = helperLat
                                                    val hLng = helperLng
                                                    val minLat = minOf(destLat, hLat)
                                                    val maxLat = maxOf(destLat, hLat)
                                                    val minLng = minOf(destLng, hLng)
                                                    val maxLng = maxOf(destLng, hLng)
                                                    val latPad = maxOf((maxLat - minLat) * 0.35, 0.004)
                                                    val lngPad = maxOf((maxLng - minLng) * 0.35, 0.004)
                                                    val bbox = BoundingBox(maxLat + latPad, maxLng + lngPad, minLat - latPad, minLng - lngPad)
                                                    map.zoomToBoundingBox(bbox, true, 140)
                                                } else {
                                                    map.controller.animateTo(GeoPoint(destLat, destLng))
                                                    map.controller.setZoom(17.0)
                                                }
                                            } catch (e: Exception) {
                                                android.util.Log.e("TrackHelperScreen", "Error re-centering map: ${e.message}")
                                            }
                                        }
                                    },
                                shadowElevation = 4.dp
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.MyLocation,
                                        contentDescription = "Re-center Map",
                                        tint = GlassTheme.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Standby Overlay when Helper has not yet pressed "Start Task"
                        if (!isTrackingActive) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = (if (isDark) Color(0xFF0F2133) else Color.White).copy(alpha = 0.92f),
                                border = BorderStroke(1.dp, GlassTheme.cardBorder),
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(horizontal = 24.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Default.LocationSearching,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(28.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Awaiting Helper Live GPS",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = GlassTheme.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "$helperName will share their live route once they tap 'Start Task'. Live OpenStreetMap is active at destination.",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp, lineHeight = 16.sp),
                                        color = GlassTheme.textSecondary,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }

                // 4. Privacy & End-to-End Scoping Notice
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    alpha = 0.82f
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = GlassTheme.accent,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Privacy Protected: Location sharing is active only during this ongoing task and terminates immediately when finished or cancelled.",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp
                            ),
                            color = GlassTheme.textSecondary
                        )
                    }
                }

                // 5. Actions: Refresh / Back to Task (Note: "Open in Google Maps" has been completely removed)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onRefresh,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, GlassTheme.cardBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = GlassTheme.chipBackground,
                            contentColor = GlassTheme.textPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Refresh",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }

                    BlackPillButton(
                        text = "Back to Task",
                        onClick = onBackClick,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Creates a crisp Requester/Destination pin drawable with a teardrop shape,
 * white border, home badge emblem, and a rounded text label pill.
 * Returns Triple(Drawable, anchorX, anchorY) so the pin tip aligns exactly with coordinates.
 */
private fun createRequesterMarkerDrawable(
    context: Context,
    label: String
): Triple<Drawable, Float, Float> {
    val density = context.resources.displayMetrics.density
    val pinRadius = 14f * density
    val pinHeight = 44f * density
    val padding = (4 * density).toInt()

    val cleanLabel = "Requester • $label".take(24)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 10f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val textBounds = Rect()
    textPaint.getTextBounds(cleanLabel, 0, cleanLabel.length, textBounds)
    val labelWidth = textBounds.width() + (18 * density).toInt()
    val labelHeight = (20 * density).toInt()

    val totalWidth = maxOf((pinRadius * 2).toInt(), labelWidth) + padding * 2
    val totalHeight = (pinHeight + labelHeight + (6 * density) + padding * 2).toInt()

    val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centerX = totalWidth / 2f
    val circleCenterY = padding + pinRadius

    // Draw Teardrop Pin Path pointing down
    val pinPath = android.graphics.Path().apply {
        arcTo(
            RectF(centerX - pinRadius, circleCenterY - pinRadius, centerX + pinRadius, circleCenterY + pinRadius),
            -180f,
            180f
        )
        lineTo(centerX, circleCenterY + pinRadius + (12f * density))
        close()
    }

    val pinPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#E11D48") // Vibrant Rose/Crimson
        style = Paint.Style.FILL
    }
    val pinStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
    }
    canvas.drawPath(pinPath, pinPaint)
    canvas.drawPath(pinPath, pinStroke)

    // Inner White Circle with Home emblem
    val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
    }
    canvas.drawCircle(centerX, circleCenterY, 8.5f * density, innerCirclePaint)

    val emblemPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#E11D48")
        textSize = 11f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val letterY = circleCenterY - ((emblemPaint.descent() + emblemPaint.ascent()) / 2)
    canvas.drawText("H", centerX, letterY, emblemPaint)

    // Tip position for accurate anchor alignment
    val tipY = circleCenterY + pinRadius + (12f * density)
    val anchorX = 0.5f
    val anchorY = tipY / totalHeight.toFloat()

    // Label Pill directly below the pin
    val labelTop = tipY + (3f * density)
    val labelRect = RectF(
        centerX - labelWidth / 2f,
        labelTop,
        centerX + labelWidth / 2f,
        labelTop + labelHeight
    )
    val labelBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0F172A")
        alpha = 235
    }
    val labelStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#38BDF8")
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }
    canvas.drawRoundRect(labelRect, 8f * density, 8f * density, labelBg)
    canvas.drawRoundRect(labelRect, 8f * density, 8f * density, labelStroke)

    val textY = labelRect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
    canvas.drawText(cleanLabel, centerX, textY, textPaint)

    return Triple(BitmapDrawable(context.resources, bitmap), anchorX, anchorY)
}

/**
 * Creates a distinct Helper avatar marker drawable with glowing cyan border,
 * helper initial, active green live dot, and name pill.
 * Returns Triple(Drawable, anchorX, anchorY) centered on the coordinate.
 */
private fun createHelperMarkerDrawable(
    context: Context,
    helperName: String
): Triple<Drawable, Float, Float> {
    val density = context.resources.displayMetrics.density
    val badgeRadius = 18f * density
    val padding = (6 * density).toInt()

    val cleanName = helperName.take(18)
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 10f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val textBounds = Rect()
    textPaint.getTextBounds(cleanName, 0, cleanName.length, textBounds)
    val labelWidth = maxOf(textBounds.width() + (24 * density).toInt(), (70 * density).toInt())
    val labelHeight = (20 * density).toInt()

    val totalWidth = maxOf((badgeRadius * 2).toInt(), labelWidth) + padding * 2
    val totalHeight = ((badgeRadius * 2) + labelHeight + (6 * density) + padding * 2).toInt()

    val bitmap = Bitmap.createBitmap(totalWidth, totalHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val centerX = totalWidth / 2f
    val centerY = padding + badgeRadius

    // Glowing Cyan Outer Ring
    val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#00C6FF")
        alpha = 80
        style = Paint.Style.STROKE
        strokeWidth = 5f * density
    }
    canvas.drawCircle(centerX, centerY, badgeRadius + 2f * density, glowPaint)

    // Main Badge Circle
    val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0284C7")
        style = Paint.Style.FILL
    }
    val badgeStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2.5f * density
    }
    canvas.drawCircle(centerX, centerY, badgeRadius, badgePaint)
    canvas.drawCircle(centerX, centerY, badgeRadius, badgeStroke)

    // Helper Initial
    val initial = helperName.trim().take(1).uppercase().ifBlank { "H" }
    val initialPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 14f * density
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    val initialY = centerY - ((initialPaint.descent() + initialPaint.ascent()) / 2)
    canvas.drawText(initial, centerX, initialY, initialPaint)

    // Green Live Indicator Dot on Badge Top-Right
    val liveDotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#10B981")
    }
    val dotStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 1.5f * density
    }
    val dotX = centerX + badgeRadius * 0.7f
    val dotY = centerY - badgeRadius * 0.7f
    canvas.drawCircle(dotX, dotY, 4.5f * density, liveDotPaint)
    canvas.drawCircle(dotX, dotY, 4.5f * density, dotStroke)

    // Center Anchor for circular badge
    val anchorX = 0.5f
    val anchorY = centerY / totalHeight.toFloat()

    // Name Label Pill below badge
    val labelTop = centerY + badgeRadius + (4f * density)
    val labelRect = RectF(
        centerX - labelWidth / 2f,
        labelTop,
        centerX + labelWidth / 2f,
        labelTop + labelHeight
    )
    val labelBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#0F172A")
        alpha = 235
    }
    val labelStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.parseColor("#00C6FF")
        style = Paint.Style.STROKE
        strokeWidth = 1.2f * density
    }
    canvas.drawRoundRect(labelRect, 8f * density, 8f * density, labelBg)
    canvas.drawRoundRect(labelRect, 8f * density, 8f * density, labelStroke)

    // Green Dot inside Label
    canvas.drawCircle(labelRect.left + (8 * density), labelRect.centerY(), 3f * density, liveDotPaint)

    val labelTextY = labelRect.centerY() - ((textPaint.descent() + textPaint.ascent()) / 2)
    canvas.drawText("$cleanName (Helper)", centerX + (4 * density), labelTextY, textPaint)

    return Triple(BitmapDrawable(context.resources, bitmap), anchorX, anchorY)
}
