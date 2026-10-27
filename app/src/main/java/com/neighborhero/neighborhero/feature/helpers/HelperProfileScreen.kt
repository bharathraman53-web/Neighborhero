package com.neighborhero.neighborhero.feature.helpers

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
            // Hero Photo Header Banner with Actual Stitch Vikram Roy Asset
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_helper_vikram),
                        contentDescription = "Vikram Roy portrait",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

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
                                    Text("Neighborhood Hero", style = MaterialTheme.typography.labelSmall, color = OnSecondary)
                                }
                            }

                            Surface(shape = RoundedCornerShape(12.dp), color = Surface.copy(alpha = 0.3f)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Verified, contentDescription = null, tint = OnPrimary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Verified ID", style = MaterialTheme.typography.labelSmall, color = OnPrimary)
                                }
                            }
                        }

                        Text(helper.name, style = MaterialTheme.typography.headlineLarge, color = OnPrimary, fontWeight = FontWeight.Bold)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryFixedDim, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${helper.neighborhood} • ${helper.distance}", style = MaterialTheme.typography.bodyMedium, color = PrimaryFixedDim)
                        }
                    }
                }
            }

            // Stats Bar Container Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Secondary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${helper.rating}", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                        }
                        Text("${helper.reviewCount} reviews", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${helper.tasksDone}", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("Tasks Done", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${helper.onTimePercentage}%", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("On-Time", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                }
            }

            // About Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("About ${helper.name.split(" ").first()}", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(helper.bio, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }
            }

            // Skills & Specialities
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Skills & Specialities", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    helper.skills.take(3).forEach { skill ->
                        Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) {
                            Text(skill, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = OnSecondaryContainer)
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onChatClick,
                    modifier = Modifier.height(52.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.ChatBubble, contentDescription = "Chat", tint = OnSurface)
                }

                PrimaryButton(
                    onClick = onRequestHelperClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.WavingHand, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Request Helper", style = MaterialTheme.typography.labelLarge)
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
        neighborhood = "Greenwood",
        distance = "0.4 miles away",
        rating = 4.9f,
        reviewCount = 128,
        tasksDone = 42,
        onTimePercentage = 99,
        bio = "Hey neighbor! I’m Vikram, a weekend DIY enthusiast and community handy-person.",
        skills = listOf("Heavy Lifting", "Furniture Assembly", "Yard Work")
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
