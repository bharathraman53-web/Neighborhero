package com.neighborhero.neighborhero.feature.safety

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun SosAndSafetyScreen(
    onNavigate: (String) -> Unit,
    onBackClick: (() -> Unit)? = null,
    onTriggerSos: ((address: String) -> Unit)? = null
) {
    val context = LocalContext.current
    var sosActive by remember { mutableStateOf(false) }

    var pingContactName by remember { mutableStateOf<String?>(null) }
    var reportModalTitle by remember { mutableStateOf<String?>(null) }
    var reportText by remember { mutableStateOf("") }
    var feedbackSnackbar by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = { AppTopBar(title = "SOS & Community Safety", onBackClick = onBackClick) },
        bottomBar = { BottomNavigationBar(currentRoute = Routes.SOS_AND_SAFETY, onNavigate = onNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Status banner if feedback available
            if (!feedbackSnackbar.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = feedbackSnackbar ?: "",
                        color = OnPrimaryContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Big Urgent SOS Callout Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = ErrorContainer),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Error, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Emergency, contentDescription = null, tint = OnError, modifier = Modifier.size(36.dp))
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Emergency SOS Dispatch", style = MaterialTheme.typography.headlineLarge, color = OnErrorContainer, fontWeight = FontWeight.Bold)
                    Text("Instant alert to verified neighborhood wardens & emergency contacts", style = MaterialTheme.typography.bodySmall, color = OnErrorContainer)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            sosActive = !sosActive
                            if (sosActive) {
                                onTriggerSos?.invoke("Maplewood District")
                                feedbackSnackbar = "Emergency SOS signal broadcasted to verified safety circle."
                            } else {
                                feedbackSnackbar = "Emergency SOS cancelled."
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Error,
                            contentColor = OnError
                        )
                    ) {
                        Text(
                            if (sosActive) "SOS SIGNAL ACTIVE - HELP EN ROUTE" else "HOLD FOR EMERGENCY SOS",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VolumeOff, contentDescription = null, tint = OnTertiaryContainer, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Silent alarm mode active (vibration only)", style = MaterialTheme.typography.bodySmall, color = OnTertiaryContainer)
                    }
                }
            }

            // Quick Reporting
            Text("Quick Reporting", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            reportModalTitle = "Report Suspicious Activity"
                            reportText = ""
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Tertiary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Suspicious Activity", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("Tap to report incident", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            reportModalTitle = "Report Hazard / Obstacle"
                            reportText = ""
                        },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Secondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Hazard / Obstacle", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("Lighting, road debris", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
            }

            // Trusted Safety Circle with Stitch Sarah & Marcus Assets
            Text("Trusted Safety Circle", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)

            // Sarah Jenkins Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_contact_sarah),
                                contentDescription = "Sarah Jenkins",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("Sarah Jenkins", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)
                            Text("Primary Contact • 0.5 miles away", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }

                    Row {
                        IconButton(onClick = { pingContactName = "Sarah Jenkins" }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Ping", tint = Secondary)
                        }
                        IconButton(onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:5552345678"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Primary)
                        }
                    }
                }
            }

            // Marcus Vance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(SurfaceContainerHigh)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_contact_marcus),
                                contentDescription = "Marcus Vance",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("Marcus Vance", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)
                            Text("Neighbor Warden • Block 4", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }

                    Row {
                        IconButton(onClick = { pingContactName = "Marcus Vance" }) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Ping", tint = Secondary)
                        }
                        IconButton(onClick = {
                            try {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:5558765432"))
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }) {
                            Icon(Icons.Default.Call, contentDescription = "Call", tint = Primary)
                        }
                    }
                }
            }
        }

        // Contact Ping Dialog
        if (pingContactName != null) {
            AlertDialog(
                onDismissRequest = { pingContactName = null },
                title = { Text("Send Location Ping?") },
                text = {
                    Text("A silent location ping with your current neighborhood coordinates (Maplewood District) will be registered for $pingContactName.")
                },
                confirmButton = {
                    Button(onClick = {
                        onTriggerSos?.invoke("Silent Location Ping to $pingContactName")
                        feedbackSnackbar = "Location ping successfully sent to $pingContactName."
                        pingContactName = null
                    }) {
                        Text("Send Ping")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pingContactName = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Incident Reporting Dialog
        if (reportModalTitle != null) {
            AlertDialog(
                onDismissRequest = { reportModalTitle = null },
                title = { Text(reportModalTitle ?: "Report") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Describe what you observed to notify nearby community wardens:")
                        OutlinedTextField(
                            value = reportText,
                            onValueChange = { reportText = it },
                            placeholder = { Text("e.g. Broken streetlamp near Elm St, suspicious vehicle...") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val desc = reportText.ifBlank { "Safety incident" }
                            onTriggerSos?.invoke("$reportModalTitle: $desc")
                            feedbackSnackbar = "$reportModalTitle submitted to community safety registry."
                            reportModalTitle = null
                        }
                    ) {
                        Text("Submit Report")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { reportModalTitle = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SosAndSafetyScreenPreview() {
    NeighborheroTheme {
        SosAndSafetyScreen(
            onNavigate = {},
            onBackClick = {}
        )
    }
}
