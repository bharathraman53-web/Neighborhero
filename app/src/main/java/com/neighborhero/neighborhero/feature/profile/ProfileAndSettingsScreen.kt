package com.neighborhero.neighborhero.feature.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ListAlt
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
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
    onLogoutClick: () -> Unit = {},
    onNavigate: (String) -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GlassTopBar(title = "Profile & Community") },
            bottomBar = { BottomNavigationBar(currentRoute = Routes.PROFILE_AND_SETTINGS, onNavigate = onNavigate) }
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

                // 1. Profile Hero Card
                GlassFocusCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar with soft ring
                        Box(
                            modifier = Modifier
                                .size(82.dp)
                                .clip(CircleShape)
                                .border(3.dp, GlassTheme.accent.copy(alpha = 0.4f), CircleShape)
                                .background(GlassTheme.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (user.avatarUrl.isNotBlank()) {
                                NetworkImage(
                                    url = user.avatarUrl,
                                    modifier = Modifier.fillMaxSize(),
                                    contentDescription = "Profile avatar"
                                ) {
                                    Text(
                                        user.fullName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                        style = MaterialTheme.typography.headlineLarge.copy(
                                            fontSize = 28.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GlassTheme.accent
                                    )
                                }
                            } else {
                                Text(
                                    user.fullName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                    style = MaterialTheme.typography.headlineLarge.copy(
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.accent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = user.fullName,
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = (-0.4).sp
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Member",
                                tint = GlassTheme.accent,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = GlassTheme.accent,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = user.neighborhood.ifBlank { "Maplewood District" },
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                color = GlassTheme.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Community Points Trophy Pill
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GlassTheme.warningBadgeBg,
                            border = BorderStroke(1.dp, GlassTheme.warningBadgeText.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = GlassTheme.warningBadgeText,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${user.points} Community Points",
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

                // 2. Metrics Trio Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        alpha = 0.85f
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${user.completedTasksCount}",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tasks Done",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = GlassTheme.textMuted
                            )
                        }
                    }

                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        alpha = 0.85f
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${user.rating} ★",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.warningBadgeText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Rating",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = GlassTheme.textMuted
                            )
                        }
                    }

                    GlassCard(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(20.dp),
                        alpha = 0.85f
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "99%",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.accent
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Reliability",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                color = GlassTheme.textMuted
                            )
                        }
                    }
                }

                // 3. Settings & Activity Navigation Group
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    alpha = 0.88f
                ) {
                    Column {
                        GlassMenuItem(
                            icon = Icons.Default.Edit,
                            title = "Edit Profile & Skills",
                            subtitle = "Bio, avatar and helper skills",
                            onClick = onEditProfileClick
                        )
                        HorizontalDivider(color = GlassTheme.cardBorder, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 8.dp))
                        GlassMenuItem(
                            icon = Icons.AutoMirrored.Filled.ListAlt,
                            title = "My Posted Requests",
                            subtitle = "Track and manage your requests",
                            onClick = onMyRequestsClick
                        )
                        HorizontalDivider(color = GlassTheme.cardBorder, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 8.dp))
                        GlassMenuItem(
                            icon = Icons.Default.TaskAlt,
                            title = "My Accepted Tasks",
                            subtitle = "Tasks you are currently helping with",
                            onClick = onMyAcceptedTasksClick
                        )
                        HorizontalDivider(color = GlassTheme.cardBorder, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 8.dp))
                        GlassMenuItem(
                            icon = Icons.Default.History,
                            title = "Task History",
                            subtitle = "Completed community tasks & evidence",
                            onClick = onTaskHistoryClick
                        )
                        HorizontalDivider(color = GlassTheme.cardBorder, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 8.dp))
                        GlassMenuItem(
                            icon = Icons.Default.Leaderboard,
                            title = "Helper Leaderboard",
                            subtitle = "Top contributors in the neighborhood",
                            onClick = onLeaderboardClick
                        )
                        HorizontalDivider(color = GlassTheme.cardBorder, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 8.dp))
                        GlassMenuItem(
                            icon = Icons.Default.Settings,
                            title = "Settings & Privacy",
                            subtitle = "Notifications, account & security",
                            onClick = onSettingsClick
                        )
                        HorizontalDivider(color = GlassTheme.cardBorder, thickness = 0.8.dp, modifier = Modifier.padding(horizontal = 8.dp))
                        GlassMenuItem(
                            icon = Icons.AutoMirrored.Filled.Logout,
                            title = "Log Out",
                            subtitle = "End your active session securely",
                            isDestructive = true,
                            onClick = { showLogoutDialog = true }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(80.dp))
            }

            if (showLogoutDialog) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = {
                        Text(
                            "Sign Out of NeighborHero?",
                            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                            color = GlassTheme.textPrimary
                        )
                    },
                    text = {
                        Text(
                            "Are you sure you want to log out? Your local session will be terminated and server authentication token will be revoked.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = GlassTheme.textSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutDialog = false
                                onLogoutClick()
                            }
                        ) {
                            Text("Log Out", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancel", color = GlassTheme.textMuted)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GlassMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(if (isDestructive) GlassTheme.warningBadgeBg else GlassTheme.chipBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isDestructive) Color(0xFFEF4444) else GlassTheme.textPrimary,
                modifier = Modifier.size(19.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (isDestructive) Color(0xFFEF4444) else GlassTheme.textPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                color = GlassTheme.textMuted
            )
        }

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = GlassTheme.textMuted,
            modifier = Modifier.size(18.dp)
        )
    }
}
