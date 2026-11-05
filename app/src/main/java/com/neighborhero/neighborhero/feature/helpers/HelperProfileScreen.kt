package com.neighborhero.neighborhero.feature.helpers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun HelperProfileScreen(
    helper: HelperProfile,
    onBackClick: () -> Unit,
    onRequestHelperClick: () -> Unit,
    onChatClick: () -> Unit
) {
    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Helper Profile",
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

                // Hero Photo Header Banner with Frosted Glass Surface
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(290.dp)
                        .shadow(8.dp, RoundedCornerShape(26.dp), spotColor = GlassShadowColor),
                    shape = RoundedCornerShape(26.dp),
                    color = Color.White.copy(alpha = 0.90f),
                    border = BorderStroke(1.dp, Color.White)
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (helper.avatarUrl.isNotBlank()) {
                            NetworkImage(
                                url = helper.avatarUrl,
                                modifier = Modifier.fillMaxSize(),
                                contentDescription = "${helper.name} portrait",
                                contentScale = ContentScale.Crop
                            ) {
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
                                            .background(
                                                Brush.linearGradient(
                                                    listOf(Color(0xFF386982), Color(0xFF4C829B))
                                                )
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = helper.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                            style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        } else if (helper.name.contains("Vikram", ignoreCase = true)) {
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
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFF386982), Color(0xFF4C829B))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = helper.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                    style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White
                                )
                            }
                        }

                        // Gradient Scrim for crisp text contrast
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Transparent,
                                            Color(0xFF0F172A).copy(alpha = 0.35f),
                                            Color(0xFF0F172A).copy(alpha = 0.90f)
                                        )
                                    )
                                )
                        )

                        // Overlaid Profile Info & Badges
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = Color.White.copy(alpha = 0.25f),
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.MilitaryTech,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            helper.title,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White
                                        )
                                    }
                                }

                                if (helper.isAvailableToday) {
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = GlassTheme.statusBadgeBg,
                                        border = BorderStroke(1.dp, GlassTheme.statusBadgeText.copy(alpha = 0.35f))
                                    ) {
                                        Text(
                                            "Available Today",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = GlassTheme.statusBadgeText
                                        )
                                    }
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    helper.name,
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontSize = 26.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = Color.White
                                )
                                if (helper.isVerified) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = "Verified Helper",
                                        tint = Color(0xFF38BDF8),
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${helper.neighborhood} (${helper.distance})",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color.White.copy(alpha = 0.90f)
                                )
                            }
                        }
                    }
                }

                // Stats Row: Rating, Tasks Done, On-Time
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        alpha = 0.82f
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFF59E0B),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${helper.rating}",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "${helper.reviewCount} reviews",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GlassTheme.textMuted
                            )
                        }
                    }

                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        alpha = 0.82f
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${helper.tasksDone}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "Tasks done",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GlassTheme.textMuted
                            )
                        }
                    }

                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        alpha = 0.82f
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "${helper.onTimePercentage}%",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.accent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                "On-time",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GlassTheme.textMuted
                            )
                        }
                    }
                }

                // About Helper Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    alpha = 0.85f
                ) {
                    Text(
                        "About ${helper.name.split(" ").firstOrNull() ?: helper.name}",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GlassTheme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        helper.bio,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        ),
                        color = GlassTheme.textSecondary
                    )
                }

                // Skills & Superpowers Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    alpha = 0.85f
                ) {
                    Text(
                        "Skills & Superpowers",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GlassTheme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        helper.skills.take(3).forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GlassTheme.chipBackground,
                                border = BorderStroke(1.dp, GlassTheme.chipBorder)
                            ) {
                                Text(
                                    skill,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    color = GlassTheme.chipText
                                )
                            }
                        }
                    }
                }

                // Action Buttons Row: Frosted Message Button + Pitch-Black Pill "Request Help"
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .shadow(4.dp, RoundedCornerShape(50), spotColor = GlassShadowColor)
                            .clickable { onChatClick() },
                        shape = RoundedCornerShape(50),
                        color = GlassTheme.cardBackground,
                        border = BorderStroke(1.dp, GlassTheme.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.ChatBubble,
                                contentDescription = null,
                                tint = GlassTheme.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Message",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                        }
                    }

                    Box(modifier = Modifier.weight(1.4f)) {
                        BlackPillButton(
                            text = "Request Help",
                            icon = Icons.Default.WavingHand,
                            onClick = onRequestHelperClick,
                            isFullWidth = true
                        )
                    }
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
