package com.neighborhero.neighborhero.feature.requests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.RequestCategory
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.components.GlassEmptyState
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.RequestCard
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.components.GlassTheme

@Composable
fun NearbyRequestsScreen(
    requests: List<HelpRequest>,
    onRequestClick: (HelpRequest) -> Unit,
    onNavigate: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All") + RequestCategory.CANONICAL

    val filtered = if (selectedCategory == "All") {
        requests
    } else {
        requests.filter { RequestCategory.matches(it.category, selectedCategory) }
    }

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Nearby Requests",
                    subtitle = "Browse community tasks near you"
                )
            },
            bottomBar = {
                BottomNavigationBar(currentRoute = Routes.NEARBY_REQUESTS, onNavigate = onNavigate)
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
                // Category Filter Capsules
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    items(categories) { cat ->
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
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
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

                if (filtered.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        GlassEmptyState(
                            icon = Icons.Default.Info,
                            title = "No Requests Found",
                            subtitle = "No active community help requests match '$selectedCategory'."
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 24.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtered) { req ->
                            RequestCard(request = req, onClick = { onRequestClick(req) })
                        }
                    }
                }
            }
        }
    }
}
