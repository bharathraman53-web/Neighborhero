package com.neighborhero.neighborhero.feature.helpers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun HelperLeaderboardScreen(
    helpers: List<HelperProfile>,
    onBackClick: () -> Unit,
    onHelperClick: (HelperProfile) -> Unit = {}
) {
    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Helper Leaderboard",
                    onBackClick = onBackClick
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    // Hero Community Champions Card
                    GlassFocusCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFEF3C7)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Text(
                                    "Community Champions",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontSize = 19.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Top local heroes helping neighbors every week.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 12.sp
                                    ),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }
                    }
                }

                itemsIndexed(helpers) { index, helper ->
                    val rank = index + 1
                    val rankColor = when (rank) {
                        1 -> Color(0xFFD97706) // Gold
                        2 -> Color(0xFF64748B) // Silver
                        3 -> Color(0xFFB45309) // Bronze
                        else -> GlassTheme.textPrimary
                    }
                    val rankBg = when (rank) {
                        1 -> Color(0xFFFEF3C7)
                        2 -> Color(0xFFF1F5F9)
                        3 -> Color(0xFFFFEDD5)
                        else -> GlassTheme.chipBackground
                    }

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        alpha = 0.85f,
                        onClick = { onHelperClick(helper) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank Pill
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = rankBg,
                                border = BorderStroke(1.dp, rankColor.copy(alpha = 0.3f)),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        "#$rank",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        ),
                                        color = rankColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                if (helper.avatarUrl.isNotBlank()) {
                                    NetworkImage(
                                        url = helper.avatarUrl,
                                        modifier = Modifier.fillMaxSize(),
                                        contentDescription = "${helper.name} avatar"
                                    ) {
                                        Text(
                                            helper.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = GlassTheme.accent
                                        )
                                    }
                                } else {
                                    Text(
                                        helper.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = GlassTheme.accent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    helper.name,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "${helper.tasksDone} tasks completed",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = GlassTheme.textMuted
                                )
                            }

                            // Rating Pill
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GlassTheme.warningBadgeBg,
                                border = BorderStroke(1.dp, GlassTheme.warningBadgeText.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "${helper.rating}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = GlassTheme.warningBadgeText
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
