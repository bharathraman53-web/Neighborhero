package com.neighborhero.neighborhero.ui.components

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.theme.*

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Home : BottomNavItem(Routes.HOME, "Home", Icons.Default.Home)
    object Requests : BottomNavItem(Routes.NEARBY_REQUESTS, "Requests", Icons.Default.Handshake)
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

    val dockColor = if (isDark) Color(0xFF0C1D2E).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.85f)
    val dockBorder = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.28f) else Color.White.copy(alpha = 0.95f)
    val activePillBg = if (isDark) Color(0xFFF8FAFC) else Color(0xFF0F172A)
    val activeTextColor = if (isDark) Color(0xFF0F172A) else Color.White
    val inactiveTextColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(26.dp),
            color = dockColor,
            border = BorderStroke(1.dp, dockBorder),
            shadowElevation = if (isDark) 4.dp else 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = currentRoute == item.route
                    val itemBg = if (selected) activePillBg else Color.Transparent

                    val iconColor by animateColorAsState(
                        targetValue = if (selected) activeTextColor else inactiveTextColor,
                        label = "NavIconColor"
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(itemBg)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                if (currentRoute != item.route) {
                                    onNavigate(item.route)
                                }
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = iconColor
                            )
                        }
                    }
                }
            }
        }
    }
}
