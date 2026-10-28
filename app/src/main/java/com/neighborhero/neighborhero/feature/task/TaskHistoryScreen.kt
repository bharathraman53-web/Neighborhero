package com.neighborhero.neighborhero.feature.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Elderly
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.Yard
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
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

    Scaffold(
        topBar = { AppTopBar(title = "History", onBackClick = onBackClick) },
        bottomBar = { BottomNavigationBar(currentRoute = Routes.TASK_HISTORY, onNavigate = onNavigate) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search & Filter Bar
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    TextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search past tasks or skills...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Outline) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(
                            "all" to "All History ($allCount)",
                            "completed" to "Completed ($completedCountComputed)",
                            "cancelled" to "Cancelled ($cancelledCountComputed)"
                        )) { (key, label) ->
                            FilterChip(
                                selected = selectedFilter == key,
                                onClick = { selectedFilter = key },
                                label = { Text(label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Primary,
                                    selectedLabelColor = OnPrimary
                                )
                            )
                        }
                    }
                }
            }

            // Summary Banner
            if (completedCount > 0 || tasks.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) {
                                    Icon(Icons.Default.WorkspacePremium, contentDescription = null, tint = OnSecondaryContainer, modifier = Modifier.padding(8.dp))
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Community Standing", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                                    val displayCount = if (completedCount > 0) completedCount else completedCountComputed
                                    Text("$displayCount tasks logged in community history", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                                Text(" ${"%.1f".format(userRating)}", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Empty State
            if (filtered.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No Past Tasks Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = OnSurface)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "No past tasks match your search query." else "Completed and cancelled tasks will appear here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Task Items
            items(filtered) { task ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(task.title, style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (task.category == "completed") SecondaryContainer else ErrorContainer
                            ) {
                                Text(
                                    task.status,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (task.category == "completed") OnSecondaryContainer else OnErrorContainer
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(task.subtitle, style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(task.date, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            if (task.rating > 0) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                                    Text(" ${task.rating}", style = MaterialTheme.typography.labelMedium, color = OnSurface)
                                }
                            }
                        }
                    }
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
