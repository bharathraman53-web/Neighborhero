package com.neighborhero.neighborhero.ui.theme

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// =====================================================================
// 1. LIGHT WATER-INSPIRED FLUID GRADIENT PALETTE
// =====================================================================
val OceanDeep = Color(0xFF386982)       // Atmospheric oceanic teal-blue (Header/Status)
val OceanTeal = Color(0xFF4C829B)       // Soft ocean water
val OceanSunlit = Color(0xFF7AA8BC)     // Sunlit water reflection
val OceanAquaMist = Color(0xFFB5D0DE)   // Gentle aqua mist
val PaleLavenderIce = Color(0xFFDCE6EF) // Pale lavender periwinkle
val IcyFoam = Color(0xFFEEF3F7)         // Soft icy water foam
val IcyWhite = Color(0xFFF7FAFD)        // Airy calm icy white

// Standard 7-Stop Fluid Water Gradient Brush (Light Mode)
val WaterFluidGradient = Brush.verticalGradient(
    0.00f to OceanDeep,
    0.16f to OceanTeal,
    0.34f to OceanSunlit,
    0.52f to OceanAquaMist,
    0.70f to PaleLavenderIce,
    0.86f to IcyFoam,
    1.00f to IcyWhite
)

val WaterLightReflection = Brush.radialGradient(
    colors = listOf(
        Color.White.copy(alpha = 0.25f),
        Color.Transparent
    ),
    center = Offset(300f, 180f),
    radius = 750f
)

// =====================================================================
// 2. DARK NIGHT-WATER FLUID GRADIENT PALETTE
// =====================================================================
val DarkOceanDeep = Color(0xFF07131F)     // Midnight abyss oceanic top
val DarkOceanMidnight = Color(0xFF0B1E2E) // Deep ocean midnight water
val DarkOceanTeal = Color(0xFF0F2B42)     // Nocturnal teal flow
val DarkOceanMarine = Color(0xFF12344E)   // Deep marine water
val DarkLavenderIce = Color(0xFF10283C)   // Deep lavender shadow
val DarkObsidianFoam = Color(0xFF091624)  // Dark ocean foam
val DarkAbyss = Color(0xFF040A10)         // Abyss deep bottom

// Standard 7-Stop Fluid Night-Water Gradient Brush (Dark Mode)
val DarkWaterFluidGradient = Brush.verticalGradient(
    0.00f to DarkOceanDeep,
    0.16f to DarkOceanMidnight,
    0.34f to DarkOceanTeal,
    0.52f to DarkOceanMarine,
    0.70f to DarkLavenderIce,
    0.86f to DarkObsidianFoam,
    1.00f to DarkAbyss
)

val DarkWaterLightReflection = Brush.radialGradient(
    colors = listOf(
        Color(0xFF38BDF8).copy(alpha = 0.12f), // Soft moonlit cyan shimmer
        Color(0xFF818CF8).copy(alpha = 0.06f), // Subtle lavender night lighting
        Color.Transparent
    ),
    center = Offset(300f, 180f),
    radius = 800f
)

// =====================================================================
// 3. PRIMARY ACTION TOKENS (LIGHT & DARK AWARE)
// =====================================================================
val Primary = Color(0xFF0F172A)          // Pitch Black / Deep Navy Pill Action
val OnPrimary = Color(0xFFFFFFFF)        // Crisp White On-Primary
val PrimaryContainer = Color(0xFFE2E8F0)
val OnPrimaryContainer = Color(0xFF0F172A)

val PrimaryFixed = Color(0xFF0F172A)
val OnPrimaryFixed = Color(0xFFFFFFFF)
val PrimaryFixedDim = Color(0xFF1E293B)
val OnPrimaryFixedVariant = Color(0xFFF1F5F9)

// =====================================================================
// 4. SECONDARY / ACCENT TOKENS
// =====================================================================
val Secondary = Color(0xFF0284C7)        // Ocean Sky Blue
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFE0F2FE)
val OnSecondaryContainer = Color(0xFF0369A1)

val SecondaryFixed = Color(0xFF0284C7)
val SecondaryFixedDim = Color(0xFF38BDF8)
val OnSecondaryFixed = Color(0xFFFFFFFF)
val OnSecondaryFixedVariant = Color(0xFFE0F2FE)

val Tertiary = Color(0xFFF59E0B)         // Warm Amber / Star Rating & Community Points
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFFEF3C7)
val OnTertiaryContainer = Color(0xFF92400E)

val TertiaryFixed = Color(0xFFD97706)
val TertiaryFixedDim = Color(0xFFFBBF24)
val OnTertiaryFixed = Color(0xFFFFFFFF)
val OnTertiaryFixedVariant = Color(0xFFFEF3C7)

// =====================================================================
// 5. CANVAS & SURFACE TOKENS (LIGHT PALETTE)
// =====================================================================
val Background = Color(0xFFF7FAFD)       // Icy Calm Canvas
val OnBackground = Color(0xFF0F172A)     // Dark Navy Text

val Surface = Color.White.copy(alpha = 0.85f)         // Frosted Glass Base Surface
val SurfaceDim = Color.White.copy(alpha = 0.65f)
val SurfaceBright = Color.White.copy(alpha = 0.95f)
val SurfaceVariant = Color.White.copy(alpha = 0.75f)  // Elevated Frosted Glass
val SurfaceContainerLowest = Color.White.copy(alpha = 0.45f)
val SurfaceContainerLow = Color.White.copy(alpha = 0.60f)
val SurfaceContainer = Color.White.copy(alpha = 0.78f) // Standard Frosted Glass Card
val SurfaceContainerHigh = Color.White.copy(alpha = 0.92f) // Focus / Hero Glass Card
val SurfaceContainerHighest = Color(0xFFFFFFFF)

val OnSurface = Color(0xFF0F172A)        // Crisp Header & Title Text (Dark Navy)
val OnSurfaceVariant = Color(0xFF334155) // Body / Description Text (Slate Navy)
val Outline = Color.White.copy(alpha = 0.85f) // Hairline Luminous White Borders
val OutlineVariant = Color.White.copy(alpha = 0.60f)

val Error = Color(0xFFE11D48)            // Modern Rose / Alert Red
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFFFE4E6)
val OnErrorContainer = Color(0xFF9F1239)

// =====================================================================
// 6. SPECIALIZED FROSTED GLASS TOKENS
// =====================================================================
// Light Glass
val GlassWhiteOpaque = Color.White.copy(alpha = 0.94f)
val GlassWhiteCard = Color.White.copy(alpha = 0.78f)
val GlassWhiteSubtle = Color.White.copy(alpha = 0.45f)
val GlassBorderLuminous = Color.White.copy(alpha = 0.90f)
val GlassBorderSubtle = Color.White.copy(alpha = 0.60f)
val GlassShadowColor = Color(0x182C4663)

// Dark Glass
val DarkGlassCardBg = Color(0xFF0E2235).copy(alpha = 0.80f)
val DarkGlassFocusBg = Color(0xFF132B42).copy(alpha = 0.92f)
val DarkGlassBorder = Color(0xFF38BDF8).copy(alpha = 0.22f)
val DarkGlassFocusBorder = Color(0xFF38BDF8).copy(alpha = 0.38f)
val DarkGlassShadow = Color(0x40000000)

// Legacy aliases
val AgenticObsidian = Color(0xFF0F172A)
val AgenticSlateCard = Color.White.copy(alpha = 0.78f)
val AgenticSlateElevated = Color.White.copy(alpha = 0.92f)
val AgenticBorder = Color.White.copy(alpha = 0.85f)
val AgenticHairline = Color.White.copy(alpha = 0.75f)
val AgenticEmerald = Color(0xFF0F766E)
val AgenticEmeraldGlow = Color(0xFF14B8A6)
val AgenticCyan = Color(0xFF0284C7)
val AgenticCyanGlow = Color(0xFF38BDF8)
val AgenticAmber = Color(0xFFF59E0B)
val TextPrimary = Color(0xFF0F172A)
val TextSecondary = Color(0xFF334155)
val TextMuted = Color(0xFF64748B)
