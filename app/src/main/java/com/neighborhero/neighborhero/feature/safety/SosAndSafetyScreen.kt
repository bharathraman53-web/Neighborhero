package com.neighborhero.neighborhero.feature.safety

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
    onBackClick: (() -> Unit)? = null
) {
    var sosActive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(title = "SOS", onBackClick = onBackClick) },
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
            // Emergency SOS Hero Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = TertiaryContainer)
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

                    Text("Immediate Assistance", style = MaterialTheme.typography.headlineMedium, color = OnTertiary, fontWeight = FontWeight.Bold)
                    Text("Press to silently alert local emergency services and your trusted circle.", style = MaterialTheme.typography.bodyMedium, color = OnTertiaryContainer)

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { sosActive = !sosActive },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Error,
                            contentColor = OnError
                        )
                    ) {
                        Text(if (sosActive) "SOS SIGNAL ACTIVE - HELP EN ROUTE" else "HOLD FOR EMERGENCY SOS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
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
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Tertiary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Suspicious Activity", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("Report unusual behavior", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
                Card(
                    modifier = Modifier.weight(1f),
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

            // Trusted Safety Circle with Actual Stitch Sarah & Marcus Assets
            Text("Trusted Safety Circle", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)

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
                        IconButton(onClick = {}) { Icon(Icons.Default.MyLocation, contentDescription = "Ping", tint = Secondary) }
                        IconButton(onClick = {}) { Icon(Icons.Default.Call, contentDescription = "Call", tint = Primary) }
                    }
                }
            }

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
                        IconButton(onClick = {}) { Icon(Icons.Default.MyLocation, contentDescription = "Ping", tint = Secondary) }
                        IconButton(onClick = {}) { Icon(Icons.Default.Call, contentDescription = "Call", tint = Primary) }
                    }
                }
            }
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
