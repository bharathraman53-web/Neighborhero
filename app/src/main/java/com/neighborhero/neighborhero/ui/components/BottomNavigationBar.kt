package com.neighborhero.neighborhero.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.theme.Primary
import com.neighborhero.neighborhero.ui.theme.SecondaryContainer
import com.neighborhero.neighborhero.ui.theme.SurfaceContainerLow

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
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Requests,
        BottomNavItem.Helpers,
        BottomNavItem.Notifications,
        BottomNavItem.Profile
    )

    NavigationBar(
        containerColor = SurfaceContainerLow
    ) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = selected,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Primary,
                    selectedTextColor = Primary,
                    indicatorColor = SecondaryContainer
                )
            )
        }
    }
}
