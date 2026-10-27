package com.neighborhero.neighborhero.feature.helpers

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun HelperLeaderboardScreen(
    helpers: List<HelperProfile>,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "Helper Leaderboard", onBackClick = onBackClick) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(PrimaryFixed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = Primary, modifier = Modifier.size(32.dp))
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column {
                            Text("Community Champions", style = MaterialTheme.typography.headlineMedium, color = OnPrimary)
                            Text("Top local heroes helping neighbors every week.", style = MaterialTheme.typography.bodySmall, color = OnPrimaryContainer)
                        }
                    }
                }
            }

            itemsIndexed(helpers) { index, helper ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("#${index + 1}", style = MaterialTheme.typography.headlineSmall, color = Primary)

                        Spacer(modifier = Modifier.width(16.dp))

                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(SecondaryContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                helper.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                style = MaterialTheme.typography.labelLarge,
                                color = OnSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(helper.name, style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                            Text("${helper.tasksDone} tasks completed", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(16.dp))
                            Text(" ${helper.rating}", style = MaterialTheme.typography.labelMedium, color = OnSurface)
                        }
                    }
                }
            }
        }
    }
}
