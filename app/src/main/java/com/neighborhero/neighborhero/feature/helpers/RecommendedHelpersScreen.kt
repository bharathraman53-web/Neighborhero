package com.neighborhero.neighborhero.feature.helpers

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassEmptyState
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.HelperCard
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground

@Composable
fun RecommendedHelpersScreen(
    helpers: List<HelperProfile>,
    requestTitle: String? = null,
    requestCategory: String? = null,
    onHelperClick: (HelperProfile) -> Unit,
    onBackClick: (() -> Unit)? = null,
    onNavigate: (String) -> Unit
) {
    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = if (!requestTitle.isNullOrBlank()) "Recommended Helpers" else "AI Recommended Helpers",
                    subtitle = if (!requestTitle.isNullOrBlank()) requestTitle else "Proactive 6-Parameter Matching",
                    onBackClick = onBackClick
                )
            },
            bottomBar = {
                if (onBackClick == null) {
                    BottomNavigationBar(currentRoute = Routes.RECOMMENDED_HELPERS, onNavigate = onNavigate)
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Intelligent Matching Radar Summary Frosted Card
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(26.dp),
                    alpha = 0.85f,
                    elevation = 6.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.accentSoft),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = GlassTheme.accent,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (!requestCategory.isNullOrBlank()) {
                                        "AI Matching for $requestCategory"
                                    } else {
                                        "6-Parameter Intelligent Match Engine"
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Text(
                                    text = "Proactively ranked using Bayesian smoothing & Haversine formula",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Parameter Weight Pills Bar
                        val weights = listOf(
                            "🎯 Skills 30%",
                            "📍 Proximity 20%",
                            "⏱ Avail 20%",
                            "⭐ Ratings 15%",
                            "🛡 Rel 10%",
                            "⚖ Fair Workload 5%"
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            weights.forEach { w ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = GlassTheme.chipBackground,
                                    border = BorderStroke(1.dp, GlassTheme.chipBorder)
                                ) {
                                    Text(
                                        text = w,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Medium
                                        ),
                                        color = GlassTheme.chipText
                                    )
                                }
                            }
                        }
                    }
                }

                if (helpers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassEmptyState(
                            icon = Icons.Default.Info,
                            title = "No Helpers Currently Available",
                            subtitle = "Registered local helpers matching this request will appear here once active."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                    ) {
                        items(helpers) { helper ->
                            HelperCard(helper = helper, onClick = { onHelperClick(helper) })
                        }
                    }
                }
            }
        }
    }
}
