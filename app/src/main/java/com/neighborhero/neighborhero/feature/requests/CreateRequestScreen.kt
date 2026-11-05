package com.neighborhero.neighborhero.feature.requests

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.RequestCategory
import com.neighborhero.neighborhero.nlp.NlpClassifier
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassTextField
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.util.LocationHelper
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun CreateRequestScreen(
    onBackClick: () -> Unit,
    targetHelperId: String? = null,
    targetHelperName: String? = null,
    onSubmitRequest: (
        title: String,
        category: String,
        description: String,
        time: String,
        location: String,
        latitude: Double?,
        longitude: Double?
    ) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val nlpClassifier = remember { NlpClassifier(context) }

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Maplewood District") }

    // Location & GPS states
    var detectedLatitude by remember { mutableStateOf<Double?>(null) }
    var detectedLongitude by remember { mutableStateOf<Double?>(null) }
    var isPreciseLocation by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }
    var locationStatusMessage by remember { mutableStateOf<String?>(null) }
    var permissionDenied by remember { mutableStateOf(false) }
    var serviceDisabled by remember { mutableStateOf(false) }

    fun fetchCurrentLocation() {
        isLocating = true
        locationStatusMessage = null
        permissionDenied = false
        serviceDisabled = false

        scope.launch {
            val result = LocationHelper.getCurrentLocation(context)
            isLocating = false
            result.onSuccess { loc ->
                detectedLatitude = loc.latitude
                detectedLongitude = loc.longitude
                isPreciseLocation = loc.isPrecise
                location = loc.addressSummary
                locationStatusMessage = "GPS Detected"
            }.onFailure { err ->
                if (err is SecurityException) {
                    permissionDenied = true
                    locationStatusMessage = err.message ?: "NeighborHero uses your location to find suitable nearby helpers."
                } else if (err.message?.contains("turned off", ignoreCase = true) == true) {
                    serviceDisabled = true
                    locationStatusMessage = err.message
                } else {
                    locationStatusMessage = err.message ?: "Failed to acquire location."
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            permissionDenied = false
            fetchCurrentLocation()
        } else {
            permissionDenied = true
            locationStatusMessage = "NeighborHero uses your location to find suitable nearby helpers."
        }
    }

    fun onUseLocationClicked() {
        if (LocationHelper.hasLocationPermission(context)) {
            if (!LocationHelper.isLocationServiceEnabled(context)) {
                serviceDisabled = true
                locationStatusMessage = "Location services are turned off. Please enable GPS."
            } else {
                fetchCurrentLocation()
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val categories = RequestCategory.CANONICAL

    // Real-time offline NLP classification
    val nlpSuggestedCategory = remember(title, description) {
        nlpClassifier.classifyRequestCategory("$title $description")
    }

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Create Help Request",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                // Page Headline
                Column {
                    Text(
                        text = "Request Neighbor Assistance",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp
                        ),
                        color = GlassTheme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Need a hand nearby? Broadcast your task or dispatch directly to a hero.",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = GlassTheme.textSecondary
                    )
                }

                // Direct Helper Banner (if targeting specific helper)
                if (!targetHelperName.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GlassTheme.cardBackground,
                        border = BorderStroke(1.dp, GlassTheme.cardBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = null,
                                    tint = GlassTheme.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Direct Request for $targetHelperName",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Text(
                                    text = "This help request will be assigned directly to $targetHelperName.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }
                    }
                }

                // Real-Time NLP Smart Suggestion Pill
                AnimatedVisibility(visible = nlpSuggestedCategory != null && nlpSuggestedCategory != selectedCategory) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GlassTheme.cardBackground,
                        border = BorderStroke(1.dp, GlassTheme.accent),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                nlpSuggestedCategory?.let { selectedCategory = it }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.accent),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI Auto-Detected: \"$nlpSuggestedCategory\"",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.accent
                                )
                                Text(
                                    text = "Tap here to automatically select this category.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Category",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GlassTheme.textPrimary
                )

                // Category Quick Filter Capsules
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) GlassTheme.chipSelectedBg else GlassTheme.chipBackground,
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) GlassTheme.chipSelectedBg else GlassTheme.chipBorder
                            ),
                            shadowElevation = if (isSelected) 4.dp else 0.dp,
                            modifier = Modifier.clickable { selectedCategory = cat }
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = cat,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) GlassTheme.chipSelectedText else GlassTheme.chipText
                                )
                                if (isSelected) {
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Box(
                                        modifier = Modifier
                                            .size(3.dp)
                                            .background(GlassTheme.chipSelectedText, CircleShape)
                                    )
                                }
                            }
                        }
                    }
                }

                // Form Fields in Frosted Glass Container
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    alpha = 0.82f,
                    elevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        // Title
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Request Title",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            GlassTextField(
                                value = title,
                                onValueChange = { title = it },
                                placeholder = "e.g. Need assistance with grocery pickup",
                                leadingIcon = Icons.Default.Title,
                                singleLine = true
                            )
                        }

                        // Description
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Detailed Description",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            GlassTextField(
                                value = description,
                                onValueChange = { description = it },
                                placeholder = "Share specific items, timing details, or tools needed...",
                                leadingIcon = Icons.Default.EditNote,
                                singleLine = false,
                                maxLines = 4
                            )
                        }

                        // Time & Date
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                "Preferred Time & Date",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            GlassTextField(
                                value = time,
                                onValueChange = { time = it },
                                placeholder = "e.g. Today, 4:00 PM",
                                leadingIcon = Icons.Default.Schedule,
                                singleLine = true
                            )
                        }

                        // Location Header & Action
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Location",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GlassTheme.chipBackground,
                                border = BorderStroke(1.dp, GlassTheme.chipBorder),
                                modifier = Modifier.clickable(enabled = !isLocating) { onUseLocationClicked() }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (isLocating) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(12.dp),
                                            strokeWidth = 2.dp,
                                            color = GlassTheme.accent
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Locating...", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), color = GlassTheme.accent)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.MyLocation,
                                            contentDescription = null,
                                            tint = GlassTheme.accent,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(5.dp))
                                        Text(
                                            text = "Use Current Location",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                            color = GlassTheme.accent
                                        )
                                    }
                                }
                            }
                        }

                        GlassTextField(
                            value = location,
                            onValueChange = {
                                location = it
                                if (it.isBlank()) {
                                    detectedLatitude = null
                                    detectedLongitude = null
                                }
                            },
                            placeholder = "e.g. Maplewood District or 123 Main St",
                            leadingIcon = Icons.Default.LocationOn,
                            singleLine = true
                        )

                        // GPS Detected Badge
                        if (detectedLatitude != null && detectedLongitude != null) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = GlassTheme.statusBadgeBg,
                                border = BorderStroke(1.dp, GlassTheme.statusBadgeText.copy(alpha = 0.35f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.GpsFixed,
                                            contentDescription = null,
                                            tint = GlassTheme.statusBadgeText,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "GPS Detected (${if (isPreciseLocation) "Precise" else "Approximate"})",
                                                style = MaterialTheme.typography.labelMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Bold),
                                                color = GlassTheme.statusBadgeText
                                            )
                                            Text(
                                                text = String.format(Locale.US, "Lat: %.4f, Lng: %.4f", detectedLatitude, detectedLongitude),
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                                color = GlassTheme.textSecondary
                                            )
                                        }
                                    }
                                    TextButton(
                                        onClick = {
                                            detectedLatitude = null
                                            detectedLongitude = null
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Text("Clear GPS", style = MaterialTheme.typography.labelSmall, color = GlassTheme.textMuted)
                                    }
                                }
                            }
                        }
                    }
                }

                // Permission Denied Explanation Banner
                if (permissionDenied) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GlassTheme.warningBadgeBg,
                        border = BorderStroke(1.dp, GlassTheme.warningBadgeText.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "NeighborHero uses your location to find suitable nearby helpers.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = GlassTheme.warningBadgeText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = {
                                        permissionLauncher.launch(
                                            arrayOf(
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_COARSE_LOCATION
                                            )
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Grant Permission", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = GlassTheme.textPrimary)
                                }
                                TextButton(
                                    onClick = { LocationHelper.openAppSettings(context) },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("App Settings", style = MaterialTheme.typography.labelSmall, color = GlassTheme.accent)
                                }
                            }
                        }
                    }
                } else if (serviceDisabled) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = GlassTheme.warningBadgeBg,
                        border = BorderStroke(1.dp, GlassTheme.warningBadgeText.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Location services are turned off on your device.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = GlassTheme.warningBadgeText
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(
                                    onClick = { LocationHelper.openLocationSettings(context) },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Enable GPS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = GlassTheme.warningBadgeText)
                                }
                                TextButton(
                                    onClick = {
                                        serviceDisabled = false
                                        fetchCurrentLocation()
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("Retry", style = MaterialTheme.typography.labelSmall, color = GlassTheme.textPrimary)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Pitch-Black Pill Publish Button
                BlackPillButton(
                    text = "Publish Help Request",
                    onClick = {
                        if (title.isNotBlank()) {
                            onSubmitRequest(
                                title,
                                selectedCategory,
                                description,
                                time,
                                location,
                                detectedLatitude,
                                detectedLongitude
                            )
                        }
                    },
                    isFullWidth = true,
                    icon = Icons.AutoMirrored.Filled.Send
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
