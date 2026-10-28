package com.neighborhero.neighborhero.feature.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.WavingHand
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun HelperProfileScreen(
    helper: HelperProfile,
    onBackClick: () -> Unit,
    onRequestHelperClick: () -> Unit,
    onChatClick: () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "Helper Profile", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Photo Header Banner with dynamic asset resolution
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    if (helper.name.contains("Vikram", ignoreCase = true)) {
                        Image(
                            painter = painterResource(id = R.drawable.img_helper_vikram),
                            contentDescription = "${helper.name} portrait",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = helper.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                style = MaterialTheme.typography.displayLarge,
                                color = OnPrimary
                            )
                        }
                    }

                    // Gradient Scrim for Text Readability
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Primary.copy(alpha = 0.4f),
                                        Primary.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    // Profile Hero Info Badges & Title
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(shape = RoundedCornerShape(12.dp), color = Secondary) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MilitaryTech, contentDescription = null, tint = OnSecondary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(helper.title, style = MaterialTheme.typography.labelSmall, color = OnSecondary)
                                }
                            }

                            if (helper.isAvailableToday) {
                                Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) {
                                    Text(
                                        "Available Today",
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OnSecondaryContainer
                                    )
                                }
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(helper.name, style = MaterialTheme.typography.headlineLarge, color = OnPrimary, fontWeight = FontWeight.Bold)
                            if (helper.isVerified) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified Helper", tint = SecondaryFixed, modifier = Modifier.size(20.dp))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryFixedDim, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${helper.neighborhood} (${helper.distance})", style = MaterialTheme.typography.bodySmall, color = PrimaryFixedDim)
                        }
                    }
                }
            }

            // Stats Row Container
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${helper.rating}", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                        }
                        Text("${helper.reviewCount} reviews", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${helper.tasksDone}", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("Tasks done", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("${helper.onTimePercentage}%", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("On-time", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
            }

            // About Helper
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About ${helper.name.split(" ").firstOrNull() ?: helper.name}", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(helper.bio, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }

            // Skills & Community Badges
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Skills & Superpowers", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        helper.skills.take(3).forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SecondaryContainer
                            ) {
                                Text(skill, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = OnSecondaryContainer)
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onChatClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SurfaceContainerHigh, contentColor = Primary)
                ) {
                    Icon(Icons.Default.ChatBubble, contentDescription = null, tint = Primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Message", style = MaterialTheme.typography.labelLarge)
                }

                PrimaryButton(
                    onClick = onRequestHelperClick,
                    modifier = Modifier.weight(1.4f)
                ) {
                    Icon(Icons.Default.WavingHand, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Help", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HelperProfileScreenPreview() {
    val sampleHelper = HelperProfile(
        id = "helper_1",
        name = "Vikram Roy",
        title = "Neighborhood Hero",
        neighborhood = "Greenwood",
        distance = "0.4 miles away",
        rating = 4.9f,
        reviewCount = 128,
        tasksDone = 42,
        onTimePercentage = 99,
        bio = "Hey neighbor! I’m Vikram, a weekend DIY enthusiast and community handy-person.",
        skills = listOf("Heavy Lifting", "Furniture Assembly", "Yard Work"),
        isVerified = true,
        isAvailableToday = true
    )
    NeighborheroTheme {
        HelperProfileScreen(
            helper = sampleHelper,
            onBackClick = {},
            onRequestHelperClick = {},
            onChatClick = {}
        )
    }
}
