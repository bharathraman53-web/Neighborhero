package com.neighborhero.neighborhero.feature.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassTextField
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.theme.LocalIsDarkTheme
import com.neighborhero.neighborhero.util.LocationHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSelectorBottomSheet(
    currentLocation: String,
    onLocationConfirmed: (newLocation: String, latitude: Double?, longitude: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isDark = LocalIsDarkTheme.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current

    var inputText by remember { mutableStateOf(currentLocation) }
    var detectedLat by remember { mutableStateOf<Double?>(null) }
    var detectedLng by remember { mutableStateOf<Double?>(null) }
    var isLocating by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorMessage by remember { mutableStateOf(false) }

    val suggestedDistricts = remember {
        listOf(
            "Maplewood District",
            "Oakridge Avenue",
            "Pinecrest",
            "Greenwood",
            "Riverside",
            "Downtown"
        )
    }

    val requestGpsLocation = {
        coroutineScope.launch {
            isLocating = true
            statusMessage = null
            isErrorMessage = false
            val result = LocationHelper.getCurrentLocation(context)
            isLocating = false
            result.onSuccess { locResult ->
                inputText = locResult.addressSummary
                detectedLat = locResult.latitude
                detectedLng = locResult.longitude
                statusMessage = "Located: ${locResult.addressSummary}"
                isErrorMessage = false
            }.onFailure { error ->
                statusMessage = error.message ?: "Could not detect location. You can enter your neighborhood manually."
                isErrorMessage = true
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            requestGpsLocation()
        } else {
            statusMessage = "Location permission denied. Please enter your neighborhood manually."
            isErrorMessage = true
        }
    }

    val onUseCurrentLocationClick = {
        focusManager.clearFocus()
        if (LocationHelper.hasLocationPermission(context)) {
            if (!LocationHelper.isLocationServiceEnabled(context)) {
                statusMessage = "GPS is turned off on your device. Please enable location services or enter manually."
                isErrorMessage = true
            } else {
                requestGpsLocation()
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

    val submitConfirmation = {
        focusManager.clearFocus()
        val target = inputText.trim().ifBlank { currentLocation.ifBlank { "Maplewood District" } }
        onLocationConfirmed(target, detectedLat, detectedLng)
        coroutineScope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isDark) Color(0xFF0F1E2E) else Color(0xFFF0F9FF),
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .width(42.dp)
                    .height(4.5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isDark) Color(0xFF38BDF8).copy(alpha = 0.4f) else Color(0xFF0284C7).copy(alpha = 0.3f))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 22.dp)
                .padding(bottom = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF132B42) else Color(0xFFE0F2FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Choose Your Location",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.3).sp
                        ),
                        color = GlassTheme.textPrimary
                    )
                    Text(
                        text = "Neighbors and AI helpers will be tailored to this area",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = GlassTheme.textSecondary
                    )
                }
            }

            // Current Active Location Indicator
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color(0xFF132B42).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.8f),
                border = BorderStroke(1.dp, if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Current: ",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GlassTheme.textMuted
                    )
                    Text(
                        text = currentLocation.ifBlank { "Maplewood District" },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Option 1: GPS "Use My Current Location" Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = !isLocating) { onUseCurrentLocationClick() },
                shape = RoundedCornerShape(20.dp),
                alpha = if (isDark) 0.88f else 0.95f
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isDark) Color(0xFF0B2538) else Color(0xFFE0F2FE)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Use My Current Location",
                                tint = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Use My Current Location",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Text(
                                text = "Detect your district automatically via GPS",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                                color = GlassTheme.textSecondary
                            )
                        }
                    }

                    if (isLocating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            strokeWidth = 2.dp,
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)
                        )
                    }
                }
            }

            // Status feedback banner
            if (statusMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isErrorMessage) Color(0xFFFFE4E6) else Color(0xFFDCFCE7),
                    border = BorderStroke(
                        1.dp,
                        if (isErrorMessage) Color(0xFFFDA4AF) else Color(0xFF86EFAC)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isErrorMessage) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = if (isErrorMessage) Color(0xFFE11D48) else Color(0xFF16A34A),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusMessage!!,
                            color = if (isErrorMessage) Color(0xFFE11D48) else Color(0xFF166534),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }
            }

            // Option 2: Manual Search / Entry Section
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Or Enter Neighborhood Manually",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GlassTheme.textPrimary
                )

                GlassTextField(
                    value = inputText,
                    onValueChange = {
                        inputText = it
                        detectedLat = null
                        detectedLng = null
                        statusMessage = null
                    },
                    placeholder = "e.g. Maplewood District, Oakridge",
                    leadingIcon = Icons.Default.Search,
                    trailingIcon = if (inputText.isNotEmpty()) {
                        {
                            IconButton(onClick = {
                                inputText = ""
                                detectedLat = null
                                detectedLng = null
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = GlassTheme.textMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    } else null,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { submitConfirmation() }
                    )
                )

                // Quick-Pick District Capsules
                Text(
                    text = "Popular Neighborhoods",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = GlassTheme.textMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestedDistricts.forEach { district ->
                        val isSelected = inputText.equals(district, ignoreCase = true)
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isSelected) {
                                if (isDark) Color(0xFF0284C7) else Color(0xFF0F172A)
                            } else {
                                if (isDark) Color(0xFF132B42) else Color.White
                            },
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) Color.Transparent else if (isDark) Color(0xFF38BDF8).copy(alpha = 0.3f) else Color.White
                            ),
                            modifier = Modifier.clickable {
                                inputText = district
                                detectedLat = null
                                detectedLng = null
                                statusMessage = null
                            }
                        ) {
                            Text(
                                text = district,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) Color.White else GlassTheme.textPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Action Buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BlackPillButton(
                    text = "Confirm & Save Location",
                    icon = Icons.Default.CheckCircle,
                    isFullWidth = true,
                    onClick = { submitConfirmation() }
                )

                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            sheetState.hide()
                            onDismiss()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = GlassTheme.textSecondary
                    )
                }
            }
        }
    }
}
