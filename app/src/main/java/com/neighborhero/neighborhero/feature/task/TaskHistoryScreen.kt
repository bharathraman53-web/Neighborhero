package com.neighborhero.neighborhero.feature.task

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
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassEmptyState
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTextField
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

data class HistoryTask(
    val title: String,
    val subtitle: String,
    val date: String,
    val rating: Float,
    val status: String,
    val category: String
)

@Composable
fun TaskHistoryScreen(
    tasks: List<HistoryTask> = emptyList(),
    userRating: Float = 5.0f,
    completedCount: Int = 0,
    onBackClick: (() -> Unit)? = null,
    onNavigate: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("all") }

    val allCount = tasks.size
    val completedCountComputed = tasks.count { it.category == "completed" }
    val cancelledCountComputed = tasks.count { it.category == "cancelled" }

    val filtered = tasks.filter {
        (selectedFilter == "all" || it.category == selectedFilter) &&
        (searchQuery.isBlank() || it.title.contains(searchQuery, ignoreCase = true))
    }

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GlassTopBar(title = "Task History", onBackClick = onBackClick) },
            bottomBar = { BottomNavigationBar(currentRoute = Routes.TASK_HISTORY, onNavigate = onNavigate) }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Search & Filter Bar
                item {
                    Spacer(modifier = Modifier.height(2.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        GlassTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "Search past tasks or skills...",
                            leadingIcon = Icons.Default.Search
                        )

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(listOf(
                                "all" to "All History ($allCount)",
                                "completed" to "Completed ($completedCountComputed)",
                                "cancelled" to "Cancelled ($cancelledCountComputed)"
                            )) { (key, label) ->
                                val isSelected = selectedFilter == key
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (isSelected) GlassTheme.chipSelectedBg else GlassTheme.chipBackground,
                                    border = BorderStroke(1.dp, if (isSelected) GlassTheme.chipSelectedBg else GlassTheme.chipBorder),
                                    modifier = Modifier.clickable { selectedFilter = key }
                                ) {
                                    Text(
                                        text = label,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (isSelected) GlassTheme.chipSelectedText else GlassTheme.chipText
                                    )
                                }
                            }
                        }
                    }
                }

                // Standing Banner
                if (completedCount > 0 || tasks.isNotEmpty()) {
                    item {
                        GlassFocusCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(22.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(GlassTheme.warningBadgeBg),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.WorkspacePremium,
                                            contentDescription = null,
                                            tint = GlassTheme.warningBadgeText,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            "Community Standing",
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = GlassTheme.textPrimary
                                        )
                                        val displayCount = if (completedCount > 0) completedCount else completedCountComputed
                                        Text(
                                            "$displayCount tasks logged in community history",
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                            color = GlassTheme.textSecondary
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = GlassTheme.warningBadgeBg,
                                    border = BorderStroke(1.dp, GlassTheme.warningBadgeText.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Star,
                                            contentDescription = null,
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "%.1f".format(userRating),
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            ),
                                            color = GlassTheme.warningBadgeText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Empty State
                if (filtered.isEmpty()) {
                    item {
                        GlassEmptyState(
                            icon = Icons.Default.History,
                            title = "No Past Tasks Found",
                            subtitle = if (searchQuery.isNotBlank()) "No past tasks match your search query." else "Completed and cancelled tasks will appear here as you participate."
                        )
                    }
                }

                // Task Items
                items(filtered) { task ->
                    val isCompleted = task.category == "completed"
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        alpha = 0.85f
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                task.title,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = if (isCompleted) GlassTheme.statusBadgeBg else GlassTheme.warningBadgeBg,
                                border = BorderStroke(1.dp, if (isCompleted) GlassTheme.statusBadgeText.copy(alpha = 0.35f) else GlassTheme.warningBadgeText.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    task.status,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = if (isCompleted) GlassTheme.statusBadgeText else GlassTheme.warningBadgeText
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            task.subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = GlassTheme.textSecondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                task.date,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GlassTheme.textMuted
                            )
                            if (task.rating > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        "${task.rating}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        ),
                                        color = GlassTheme.textPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun TaskHistoryScreenPreview() {
    NeighborheroTheme {
        TaskHistoryScreen(
            onBackClick = {},
            onNavigate = {}
        )
    }
}
