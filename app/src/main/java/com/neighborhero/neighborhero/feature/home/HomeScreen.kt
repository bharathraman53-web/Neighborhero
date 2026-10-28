package com.neighborhero.neighborhero.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.components.HelperCard
import com.neighborhero.neighborhero.ui.components.RequestCard
import com.neighborhero.neighborhero.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    requests: List<HelpRequest>,
    helpers: List<HelperProfile>,
    onRequestClick: (HelpRequest) -> Unit,
    onHelperClick: (HelperProfile) -> Unit,
    onCreateRequestClick: () -> Unit,
    onSosClick: () -> Unit,
    onNavigate: (String) -> Unit,
    onActiveTaskClick: (HelpRequest?) -> Unit = { onNavigate(Routes.ACTIVE_TASK) }
) {
    val activeTask = requests.firstOrNull { it.status == RequestStatus.IN_PROGRESS }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NeighborHero", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                        Text("Maplewood District", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }
                },
                actions = {
                    IconButton(onClick = onSosClick) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ErrorContainer, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Emergency, contentDescription = "SOS", tint = OnErrorContainer)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        },
        bottomBar = {
            BottomNavigationBar(currentRoute = Routes.HOME, onNavigate = onNavigate)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateRequestClick,
                containerColor = Primary,
                contentColor = OnPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create Request")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                if (activeTask != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clickable { onActiveTaskClick(activeTask) },
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(Primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Handshake, contentDescription = null, tint = OnPrimary)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Active Community Task", style = MaterialTheme.typography.labelSmall, color = OnPrimaryContainer)
                                Text(
                                    text = activeTask.title,
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = OnPrimary
                                )
                                Text(
                                    text = activeTask.requestedTime,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = PrimaryFixedDim
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = SecondaryContainer
                            ) {
                                Text(
                                    "In Progress",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = OnSecondaryContainer
                                )
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .background(PrimaryFixed, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Handshake, contentDescription = null, tint = Primary)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text("Community Dashboard", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                                Text("No active tasks in progress. Lend a hand or request help below!", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Nearby Help Requests", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                    TextButton(onClick = { onNavigate(Routes.NEARBY_REQUESTS) }) {
                        Text("See All", color = Secondary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (requests.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Outline, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Help Requests Nearby", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("No requests posted yet. Tap '+' to create one!", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        }
                    }
                }
            } else {
                items(requests.take(2)) { req ->
                    RequestCard(request = req, onClick = { onRequestClick(req) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Recommended Local Helpers", style = MaterialTheme.typography.headlineMedium, color = OnSurface)
                    TextButton(onClick = { onNavigate(Routes.RECOMMENDED_HELPERS) }) {
                        Text("See All", color = Secondary, style = MaterialTheme.typography.labelLarge)
                    }
                }
            }

            if (helpers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Outline, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No Helpers Listed Yet", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Registered neighbors will appear here.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                        }
                    }
                }
            } else {
                items(helpers.take(2)) { helper ->
                    HelperCard(helper = helper, onClick = { onHelperClick(helper) })
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
