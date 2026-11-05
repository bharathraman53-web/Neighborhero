package com.neighborhero.neighborhero.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val LocalIsDarkTheme = compositionLocalOf { false }

val GlassTheme = com.neighborhero.neighborhero.ui.components.GlassTheme

private val WaterGlassColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    secondaryContainer = SecondaryContainer,
    onSecondaryContainer = OnSecondaryContainer,
    tertiary = Tertiary,
    onTertiary = OnTertiary,
    tertiaryContainer = TertiaryContainer,
    onTertiaryContainer = OnTertiaryContainer,
    background = Background,
    onBackground = OnBackground,
    surface = Surface,
    onSurface = OnSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline,
    outlineVariant = OutlineVariant,
    error = Error,
    onError = OnError,
    errorContainer = ErrorContainer,
    onErrorContainer = OnErrorContainer
)

private val WaterGlassDarkColorScheme = darkColorScheme(
    primary = Color(0xFFF8FAFC),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF132B42),
    onPrimaryContainer = Color(0xFFE2E8F0),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF07131F),
    secondaryContainer = Color(0xFF0E253A),
    onSecondaryContainer = Color(0xFF7DD3FC),
    tertiary = Color(0xFFFBBF24),
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = Color(0xFF35240E),
    onTertiaryContainer = Color(0xFFFDE68A),
    background = Color(0xFF040A10),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0E2235).copy(alpha = 0.85f),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF142B44).copy(alpha = 0.90f),
    onSurfaceVariant = Color(0xFFCBD5E1),
    outline = Color(0xFF38BDF8).copy(alpha = 0.28f),
    outlineVariant = Color(0xFF38BDF8).copy(alpha = 0.18f),
    error = Color(0xFFFB7185),
    onError = Color(0xFF4C0519),
    errorContainer = Color(0xFF3E121F),
    onErrorContainer = Color(0xFFFECDD3)
)

@Composable
fun NeighborheroTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                @Suppress("DEPRECATION")
                window.statusBarColor = android.graphics.Color.TRANSPARENT
                @Suppress("DEPRECATION")
                window.navigationBarColor = android.graphics.Color.TRANSPARENT
                val controller = WindowCompat.getInsetsController(window, view)
                // White status bar icons over the deep oceanic teal / midnight ocean top gradient
                controller.isAppearanceLightStatusBars = false
                // Navigation bar icons: dark icons in light mode over icy white bottom, light icons in dark mode
                controller.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    CompositionLocalProvider(LocalIsDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) WaterGlassDarkColorScheme else WaterGlassColorScheme,
            typography = Typography,
            content = content
        )
    }
}
