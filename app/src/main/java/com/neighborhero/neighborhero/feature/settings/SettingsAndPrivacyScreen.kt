package com.neighborhero.neighborhero.feature.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun SettingsAndPrivacyScreen(
    onBackClick: () -> Unit
) {
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
                        leadingContent = { Icon(Icons.Default.Notifications, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Privacy & Location Sharing") },
                        leadingContent = { Icon(Icons.Default.PrivacyTip, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Account Security") },
                        leadingContent = { Icon(Icons.Default.Lock, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                    HorizontalDivider()
                    ListItem(
                        headlineContent = { Text("Help & Support Center") },
                        leadingContent = { Icon(Icons.Default.Help, contentDescription = null, tint = Primary) },
                        trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                        colors = ListItemDefaults.colors(containerColor = SurfaceContainerLow)
                    )
                }
            }
        }
    }
}
