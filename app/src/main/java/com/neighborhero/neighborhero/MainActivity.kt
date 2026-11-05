package com.neighborhero.neighborhero

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import com.neighborhero.neighborhero.navigation.AppNavigation
import com.neighborhero.neighborhero.ui.theme.AppThemeMode
import com.neighborhero.neighborhero.ui.theme.NeighborheroTheme
import com.neighborhero.neighborhero.ui.theme.ThemeManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        RepositoryProvider.init(applicationContext)
        ThemeManager.init(applicationContext)
        enableEdgeToEdge()
        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            val isDarkTheme = themeMode == AppThemeMode.DARK

            NeighborheroTheme(darkTheme = isDarkTheme) {
                AppNavigation()
            }
        }
    }
}
