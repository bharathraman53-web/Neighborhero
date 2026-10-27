package com.neighborhero.neighborhero.feature.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun ProfileAndSettingsScreen(
    user: User,
    onEditProfileClick: () -> Unit,
    onMyRequestsClick: () -> Unit,
    onMyAcceptedTasksClick: () -> Unit,
    onTaskHistoryClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "Profile & Community") },
        bottomBar = { BottomNavigationBar(currentRoute = Routes.PROFILE_AND_SETTINGS, onNavigate = onNavigate) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(PrimaryFixed, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            user.fullName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                            style = MaterialTheme.typography.headlineLarge,
                            color = OnPrimaryFixed
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(user.fullName, style = MaterialTheme.typography.headlineLarge, color = OnPrimary)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryFixedDim, modifier = Modifier.size(16.dp))
                        Text(user.neighborhood, style = MaterialTheme.typography.bodySmall, color = PrimaryFixedDim)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Surface(shape = RoundedCornerShape(12.dp), color = Primary) {
                            Text("${user.points} pts", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelMedium, color = OnPrimary)
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = Primary) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                                Text(" ${user.rating}", style = MaterialTheme.typography.labelMedium, color = OnPrimary)
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Edit Profile") },
                        leadingContent = { Icon(Icons.Default.Edit, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onEditProfileClick() },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("My Posted Requests") },
                        leadingContent = { Icon(Icons.AutoMirrored.Filled.ListAlt, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMyRequestsClick() },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("My Accepted Tasks") },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onMyAcceptedTasksClick() },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Task History") },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onTaskHistoryClick() },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Helper Leaderboard") },
                        leadingContent = { Icon(Icons.Default.Leaderboard, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onLeaderboardClick() },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Settings & Privacy") },
                        leadingContent = { Icon(Icons.Default.Settings, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSettingsClick() },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                }
            }
        }
    }
}
