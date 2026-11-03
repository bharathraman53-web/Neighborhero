package com.neighborhero.neighborhero.feature.requests

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.RequestCategory
import com.neighborhero.neighborhero.nlp.NlpClassifier
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*
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

    Scaffold(
        topBar = { AppTopBar(title = "Create Request", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("Ask your neighbors", style = MaterialTheme.typography.headlineLarge, color = OnSurface)
                Text("Need a hand with something local? Drop your request below.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }

            if (!targetHelperName.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SecondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = OnSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Direct Request for $targetHelperName",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = OnSecondaryContainer
                            )
                            Text(
                                text = "This help request will be assigned directly to $targetHelperName.",
                                style = MaterialTheme.typography.bodySmall,
                                color = OnSecondaryContainer
                            )
                        }
                    }
                }
            }

            // NLP Smart Suggestion Pill
            AnimatedVisibility(visible = nlpSuggestedCategory != null && nlpSuggestedCategory != selectedCategory) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryFixed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            nlpSuggestedCategory?.let { selectedCategory = it }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OnPrimary, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Smart Suggestion: Auto-detected \"$nlpSuggestedCategory\". Tap to select.",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnPrimaryFixed
                        )
                    }
                }
            }

            Text("Select Category", style = MaterialTheme.typography.labelLarge, color = OnSurface)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer,
                            selectedLabelColor = OnPrimaryContainer
                        )
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Request Title") },
                placeholder = { Text("e.g., Need help picking up groceries") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Description") },
                placeholder = { Text("Share specific details or tools needed...") },
                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Preferred Time & Date") },
                placeholder = { Text("e.g. Today, 4:00 PM") },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // Location Header & Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Location", style = MaterialTheme.typography.labelLarge, color = OnSurface)
                OutlinedButton(
                    onClick = { onUseLocationClicked() },
                    enabled = !isLocating,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(14.dp),
                            strokeWidth = 2.dp,
                            color = Primary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Locating...", style = MaterialTheme.typography.labelMedium)
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Use My Current Location", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            OutlinedTextField(
                value = location,
                onValueChange = {
                    location = it
                    if (it.isBlank()) {
                        detectedLatitude = null
                        detectedLongitude = null
                    }
                },
                label = { Text("Neighborhood or Address") },
                placeholder = { Text("e.g. Maplewood District or 123 Main St") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            // GPS Detected Badge
            if (detectedLatitude != null && detectedLongitude != null) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
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
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "GPS Detected (${if (isPreciseLocation) "Precise" else "Approximate"})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = String.format(Locale.US, "%.4f, %.4f", detectedLatitude, detectedLongitude),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
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
                            Text("Clear GPS", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Permission Denied Explanation Banner
            if (permissionDenied) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "NeighborHero uses your location to find suitable nearby helpers.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer
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
                                Text("Grant Permission", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(
                                onClick = { LocationHelper.openAppSettings(context) },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("App Settings", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            } else if (serviceDisabled) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Location services are turned off on your device.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextButton(
                                onClick = { LocationHelper.openLocationSettings(context) },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Enable GPS", style = MaterialTheme.typography.labelSmall)
                            }
                            TextButton(
                                onClick = {
                                    serviceDisabled = false
                                    fetchCurrentLocation()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp)
                            ) {
                                Text("Retry", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            } else if (locationStatusMessage != null && detectedLatitude == null && !isLocating) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = locationStatusMessage ?: "Could not detect location.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(
                            onClick = { fetchCurrentLocation() },
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            Text("Retry", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(onClick = {
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
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish Request", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateRequestScreenPreview() {
    NeighborheroTheme {
        CreateRequestScreen(
            onBackClick = {},
            onSubmitRequest = { _, _, _, _, _, _, _ -> }
        )
    }
}
