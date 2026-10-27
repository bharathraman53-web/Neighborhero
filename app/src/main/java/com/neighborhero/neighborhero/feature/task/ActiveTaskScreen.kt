package com.neighborhero.neighborhero.feature.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun ActiveTaskScreen(
    onBackClick: () -> Unit,
    onChatClick: () -> Unit,
    onSubmitCompletionClick: () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "Active Task", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) {
                            Text("Task In Progress", modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = OnSecondaryContainer)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Weekly Groceries Pickup", style = MaterialTheme.typography.headlineMedium, color = OnPrimary)

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryFixedDim)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("42 Maplewood Drive", style = MaterialTheme.typography.bodyMedium, color = PrimaryFixedDim)
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
                                    .background(PrimaryFixed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("EV", style = MaterialTheme.typography.titleMedium, color = OnPrimaryFixed)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text("Eleanor Vance", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                                Text("Requester Neighbor", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }

                        Row {
                            IconButton(onClick = onChatClick) {
                                Icon(Icons.Default.Chat, contentDescription = "Chat", tint = Primary)
                            }
                            IconButton(onClick = {}) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = Primary)
                            }
                        }
                    }
                }
            }

            PrimaryButton(onClick = onSubmitCompletionClick) {
                Icon(Icons.Default.CheckCircle, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Submit Task Completion", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
