package com.neighborhero.neighborhero.feature.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.NotificationItem
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassEmptyState
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun NotificationsScreen(
    notifications: List<NotificationItem>,
    onNavigate: (String) -> Unit,
    onNotificationClick: ((NotificationItem) -> Unit)? = null,
    onMarkAllReadClick: (() -> Unit)? = null,
    onClearAllClick: (() -> Unit)? = null
) {
    val unreadCount = notifications.count { !it.isRead }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = {
                Text(
                    text = "Clear Notifications",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = GlassTheme.textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to clear all notifications?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GlassTheme.textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        onClearAllClick?.invoke()
                    }
                ) {
                    Text(
                        text = "Clear All",
                        color = Color(0xFFEF4444),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancel", color = GlassTheme.textMuted)
                }
            }
        )
    }

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Notifications",
                    actions = {
                        if (unreadCount > 0 && onMarkAllReadClick != null) {
                            TextButton(onClick = onMarkAllReadClick) {
                                Text(
                                    "Mark read",
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                        if (notifications.isNotEmpty() && onClearAllClick != null) {
                            TextButton(onClick = { showClearConfirmDialog = true }) {
                                Text(
                                    "Clear all",
                                    color = Color.White.copy(alpha = 0.85f),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }
                )
            },
            bottomBar = { BottomNavigationBar(currentRoute = Routes.NOTIFICATIONS, onNavigate = onNavigate) }
        ) { innerPadding ->
            if (notifications.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassEmptyState(
                        icon = Icons.Default.Notifications,
                        title = "No Notifications",
                        subtitle = "Task updates and community alerts will appear here in real time."
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                    items(notifications, key = { it.id }) { item ->
                        val isUnread = !item.isRead
                        GlassCard(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onNotificationClick?.invoke(item) },
                            shape = RoundedCornerShape(20.dp),
                            alpha = if (isUnread) 0.94f else 0.82f,
                            borderAlpha = if (isUnread) 1.0f else 0.80f
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(if (isUnread) GlassTheme.accent.copy(alpha = 0.15f) else GlassTheme.chipBackground),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isUnread) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                                        contentDescription = null,
                                        tint = if (isUnread) GlassTheme.accent else GlassTheme.textMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = item.title,
                                            style = MaterialTheme.typography.labelLarge.copy(
                                                fontSize = 14.sp,
                                                fontWeight = if (isUnread) FontWeight.Bold else FontWeight.SemiBold
                                            ),
                                            color = GlassTheme.textPrimary
                                        )
                                        Text(
                                            text = item.timeAgo,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = GlassTheme.textMuted
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = item.message,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        ),
                                        color = GlassTheme.textSecondary
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = GlassTheme.pillButtonBg,
                                            modifier = Modifier.clickable { onNotificationClick?.invoke(item) }
                                        ) {
                                            Text(
                                                "View",
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = GlassTheme.pillButtonText
                                            )
                                        }
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
}
