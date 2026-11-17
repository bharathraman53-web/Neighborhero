package com.neighborhero.neighborhero.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.theme.*

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Routes.HOME, "Home", Icons.Default.Home)
    object Requests : BottomNavItem(Routes.NEARBY_REQUESTS, "Requests", Icons.Default.Podcasts)
    object Helpers : BottomNavItem(Routes.RECOMMENDED_HELPERS, "Helpers", Icons.Default.People)
    object Notifications : BottomNavItem(Routes.NOTIFICATIONS, "Alerts", Icons.Default.Notifications)
    object Profile : BottomNavItem(Routes.PROFILE_AND_SETTINGS, "Profile", Icons.Default.Person)
}

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Requests,
        BottomNavItem.Helpers,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )

    val dockColor = if (isDark) Color(0xFF0C1D2E).copy(alpha = 0.94f) else Color.White.copy(alpha = 0.88f)
    val dockBorder = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.30f) else Color.White.copy(alpha = 0.95f)
    val activePillBg = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0B1528)
    val activeTextColor = if (isDark) Color(0xFF0B1528) else Color.White
    val inactiveTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF1E293B)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(36.dp),
            color = dockColor,
            border = BorderStroke(1.dp, dockBorder),
            shadowElevation = if (isDark) 6.dp else 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route

                    val iconColor by animateColorAsState(
                        targetValue = if (selected) activeTextColor else inactiveTextColor,
                        animationSpec = tween(durationMillis = 200),
                        label = "NavIconColor"
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                role = Role.Tab
                            ) {
                                if (currentRoute != item.route) {
                                    onNavigate(item.route)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selected) {
                            Box(
                                modifier = Modifier
                                    .size(width = 58.dp, height = 44.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(activePillBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(width = 48.dp, height = 48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.title,
                                    tint = iconColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
