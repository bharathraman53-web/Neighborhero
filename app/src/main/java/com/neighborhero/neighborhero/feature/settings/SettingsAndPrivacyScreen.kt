package com.neighborhero.neighborhero.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun SettingsAndPrivacyScreen(
    onBackClick: () -> Unit,
    onLogoutClick: (() -> Unit)? = null
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showPushDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showSecurityDialog by remember { mutableStateOf(false) }
    var showHelpDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { AppTopBar(title = "Settings & Privacy", onBackClick = onBackClick) }
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
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column {
                    ListItem(
                        headlineContent = { Text("Push Notifications") },
                        supportingContent = { Text("In-app notifications enabled", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant) },
                        leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPushDialog = true },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Privacy & Location Sharing") },
                        supportingContent = { Text("Neighborhood-level visibility", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant) },
                        leadingContent = { Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showPrivacyDialog = true },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Account Security") },
                        supportingContent = { Text("Token authentication active", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant) },
                        leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showSecurityDialog = true },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Help & Support Center") },
                        supportingContent = { Text("FAQs, guidelines & emergency contacts", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant) },
                        leadingContent = { Icon(Icons.Default.Help, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showHelpDialog = true },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )

                    if (onLogoutClick != null) {
                        HorizontalDivider()
                        ListItem(
                            headlineContent = { Text("Log Out", color = MaterialTheme.colorScheme.error) },
                            leadingContent = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log Out", tint = MaterialTheme.colorScheme.error) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showLogoutDialog = true },
                            colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                        )
                    }
                }
            }

            if (showPushDialog) {
                AlertDialog(
                    onDismissRequest = { showPushDialog = false },
                    title = { Text("Notification Preferences") },
                    text = {
                        Text(
                            "• In-App Alerts: Active. You receive in-app notifications and real-time polling updates for task requests, chats, and completion verifications.\n\n" +
                            "• Device Push Notifications: External system push services (Firebase Cloud Messaging) are not configured for this local development build. All alerts are delivered directly in-app."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showPushDialog = false }) {
                            Text("OK")
                        }
                    }
                )
            }

            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    title = { Text("Privacy & Location Sharing") },
                    text = {
                        Text(
                            "NeighborHero protects your privacy:\n\n" +
                            "• Your exact GPS coordinates are never broadcast to other users.\n" +
                            "• Neighbors only see your approximate neighborhood area (e.g. Maplewood District) and relative distance.\n" +
                            "• Your phone number is only shared with an accepted task helper or requester during an active task."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) {
                            Text("Got it")
                        }
                    }
                )
            }

            if (showSecurityDialog) {
                AlertDialog(
                    onDismissRequest = { showSecurityDialog = false },
                    title = { Text("Account Security") },
                    text = {
                        Text(
                            "Security Information:\n\n" +
                            "• Passwords are encrypted using salted bcrypt hashes.\n" +
                            "• Communication is secured with 64-character bearer tokens.\n" +
                            "• Task completion proof images are stored in protected storage with participant-only authorization checks."
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showSecurityDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    title = { Text("Help & Support Center") },
                    text = {
                        Text(
                            "NeighborHero Community Support:\n\n" +
                            "• Emergency: In case of immediate physical danger, dial 911 or your local emergency line.\n" +
                            "• Community Guidelines: Be courteous, confirm task scope before starting, and upload photo proof upon completion.\n" +
                            "• Support Contact: support@neighborhero.local"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("Close")
                        }
                    }
                )
            }

            if (showLogoutDialog && onLogoutClick != null) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    title = { Text("Sign Out of NeighborHero?") },
                    text = { Text("Are you sure you want to log out? Your local session will be terminated and server authentication token will be revoked.") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showLogoutDialog = false
                                onLogoutClick()
                            }
                        ) {
                            Text("Log Out", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLogoutDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}
