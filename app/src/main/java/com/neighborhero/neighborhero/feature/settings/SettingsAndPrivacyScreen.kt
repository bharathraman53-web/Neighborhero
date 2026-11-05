package com.neighborhero.neighborhero.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
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
import com.neighborhero.neighborhero.ui.components.CompactThemeSwitcher
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
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

    val currentThemeMode by ThemeManager.themeMode.collectAsState()
    val isDark = LocalIsDarkTheme.current

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GlassTopBar(title = "Settings & Privacy", onBackClick = onBackClick) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // =================================================================
                // 1. SIMPLE & COMPACT THEME SELECTOR (NORMAL | DARK)
                // =================================================================
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    alpha = 0.88f
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) Color(0xFF132B42) else Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Text(
                                text = "Theme",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                        }

                        // Compact side-by-side [ ☀ Normal | 🌙 Dark ]
                        CompactThemeSwitcher(
                            currentMode = currentThemeMode,
                            onModeChanged = { ThemeManager.setThemeMode(it) }
                        )
                    }
                }

                // =================================================================
                // 2. PRIVACY, SECURITY, NOTIFICATIONS & SUPPORT OPTIONS
                // =================================================================
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    alpha = 0.88f
                ) {
                    Column {
                        SettingsRow(
                            icon = Icons.Default.Notifications,
                            title = "Push Notifications",
                            subtitle = "In-app notifications enabled",
                            onClick = { showPushDialog = true }
                        )
                        HorizontalDivider(
                            color = GlassTheme.divider,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        SettingsRow(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy & Location Sharing",
                            subtitle = "Neighborhood-level visibility",
                            onClick = { showPrivacyDialog = true }
                        )
                        HorizontalDivider(
                            color = GlassTheme.divider,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        SettingsRow(
                            icon = Icons.Default.Lock,
                            title = "Account Security",
                            subtitle = "Token authentication active",
                            onClick = { showSecurityDialog = true }
                        )
                        HorizontalDivider(
                            color = GlassTheme.divider,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        SettingsRow(
                            icon = Icons.AutoMirrored.Filled.Help,
                            title = "Help & Support Center",
                            subtitle = "FAQs, guidelines & emergency contacts",
                            onClick = { showHelpDialog = true }
                        )

                        if (onLogoutClick != null) {
                            HorizontalDivider(
                                color = GlassTheme.divider,
                                thickness = 0.8.dp,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            SettingsRow(
                                icon = Icons.AutoMirrored.Filled.Logout,
                                title = "Log Out",
                                subtitle = "End active session",
                                isDestructive = true,
                                onClick = { showLogoutDialog = true }
                            )
                        }
                    }
                }
            }

            // =================================================================
            // 3. DIALOGS (THEME ADAPTIVE)
            // =================================================================
            val dialogBg = if (isDark) Color(0xFF132B42) else Color.White

            if (showPushDialog) {
                AlertDialog(
                    onDismissRequest = { showPushDialog = false },
                    containerColor = dialogBg,
                    title = {
                        Text(
                            "Notification Preferences",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassTheme.textPrimary
                        )
                    },
                    text = {
                        Text(
                            "• In-App Alerts: Active. You receive in-app notifications and real-time polling updates for task requests, chats, and completion verifications.\n\n" +
                            "• Device Push Notifications: External system push services (Firebase Cloud Messaging) are not configured for this local development build. All alerts are delivered directly in-app.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = GlassTheme.textSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showPushDialog = false }) {
                            Text("OK", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showPrivacyDialog) {
                AlertDialog(
                    onDismissRequest = { showPrivacyDialog = false },
                    containerColor = dialogBg,
                    title = {
                        Text(
                            "Privacy & Location Sharing",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassTheme.textPrimary
                        )
                    },
                    text = {
                        Text(
                            "NeighborHero protects your privacy:\n\n" +
                            "• Your exact GPS coordinates are never broadcast to other users.\n" +
                            "• Neighbors only see your approximate neighborhood area and relative distance.\n" +
                            "• Your phone number is only shared with an accepted task helper or requester during an active task.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = GlassTheme.textSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showPrivacyDialog = false }) {
                            Text("Got it", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showSecurityDialog) {
                AlertDialog(
                    onDismissRequest = { showSecurityDialog = false },
                    containerColor = dialogBg,
                    title = {
                        Text(
                            "Account Security",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassTheme.textPrimary
                        )
                    },
                    text = {
                        Text(
                            "Security Information:\n\n" +
                            "• Passwords are encrypted using salted bcrypt hashes.\n" +
                            "• Communication is secured with 64-character bearer tokens.\n" +
                            "• Task completion proof images are stored in protected storage with participant-only authorization checks.",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = GlassTheme.textSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showSecurityDialog = false }) {
                            Text("Close", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showHelpDialog) {
                AlertDialog(
                    onDismissRequest = { showHelpDialog = false },
                    containerColor = dialogBg,
                    title = {
                        Text(
                            "Help & Support Center",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GlassTheme.textPrimary
                        )
                    },
                    text = {
                        Text(
                            "NeighborHero Community Support:\n\n" +
                            "• Emergency: In case of immediate physical danger, dial 911 or your local emergency line.\n" +
                            "• Community Guidelines: Be courteous, confirm task scope before starting, and upload photo proof upon completion.\n" +
                            "• Support Contact: support@neighborhero.local",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp, lineHeight = 18.sp),
                            color = GlassTheme.textSecondary
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = { showHelpDialog = false }) {
                            Text("Close", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            if (showLogoutDialog && onLogoutClick != null) {
                AlertDialog(
                    onDismissRequest = { showLogoutDialog = false },
                    containerColor = dialogBg,
                    title = {
                        Text(
                            "Sign Out of NeighborHero?",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
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
                            Text("Log Out", color = Color(0xFFE11D48), fontWeight = FontWeight.Bold)
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
private fun SettingsRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val iconBg = if (isDestructive) {
        if (isDark) Color(0xFF3B1219) else Color(0xFFFFE4E6)
    } else {
        if (isDark) Color(0xFF142B42) else Color(0xFFF1F5F9)
    }
    val iconTint = if (isDestructive) Color(0xFFE11D48) else Color(0xFF38BDF8)
    val titleColor = if (isDestructive) Color(0xFFE11D48) else GlassTheme.textPrimary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
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
                color = titleColor
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
