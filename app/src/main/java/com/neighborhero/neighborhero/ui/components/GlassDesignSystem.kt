package com.neighborhero.neighborhero.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.ui.theme.*

/**
 * Centralized theme-aware tokens for the water-inspired fluid design system.
 * Adapts dynamically between Normal (sunlit ocean water) and Dark (nocturnal ocean abyss).
 */
object GlassTheme {
    val isDark: Boolean
        @Composable get() = LocalIsDarkTheme.current

    val backgroundBrush: Brush
        @Composable get() = if (isDark) DarkWaterFluidGradient else WaterFluidGradient

    val reflectionBrush: Brush
        @Composable get() = if (isDark) DarkWaterLightReflection else WaterLightReflection

    // Translucent Card Surfaces
    val cardBackground: Color
        @Composable get() = if (isDark) Color(0xFF0E2235).copy(alpha = 0.82f) else Color.White.copy(alpha = 0.82f)

    val cardBorder: Color
        @Composable get() = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.22f) else Color.White.copy(alpha = 0.90f)

    val focusCardBackground: Color
        @Composable get() = if (isDark) Color(0xFF132B42).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.94f)

    val focusCardBorder: Color
        @Composable get() = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.38f) else Color.White

    // Typography (Complies strictly with Dark Mode colour rules)
    val textPrimary: Color
        @Composable get() = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)

    val textSecondary: Color
        @Composable get() = if (isDark) Color(0xFFCBD5E1) else Color(0xFF475569)

    val textMuted: Color
        @Composable get() = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)

    // Accents
    val accent: Color
        @Composable get() = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    val accentSoft: Color
        @Composable get() = if (isDark) Color(0xFF0C2438) else Color(0xFFE0F2FE)

    // Buttons
    val pillButtonBg: Color
        @Composable get() = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)

    val pillButtonText: Color
        @Composable get() = if (isDark) Color(0xFF0F172A) else Color.White

    val secondaryButtonBg: Color
        @Composable get() = if (isDark) Color(0xFF14283D) else Color.White.copy(alpha = 0.85f)

    val secondaryButtonBorder: Color
        @Composable get() = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.30f) else Color.White

    val secondaryButtonText: Color
        @Composable get() = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)

    // Chips / Badges
    val chipBackground: Color
        @Composable get() = if (isDark) Color(0xFF142638) else Color(0xFFF1F5F9)

    val chipBorder: Color
        @Composable get() = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color(0xFFE2E8F0)

    val chipText: Color
        @Composable get() = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)

    val chipSelectedBg: Color
        @Composable get() = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)

    val chipSelectedText: Color
        @Composable get() = if (isDark) Color(0xFF0F172A) else Color.White

    // Divider
    val divider: Color
        @Composable get() = if (isDark) Color(0x2E38BDF8) else Color(0x1F0F172A)

    // Input Fields
    val inputContainer: Color
        @Composable get() = if (isDark) Color(0xFF0E2235).copy(alpha = 0.90f) else Color.White.copy(alpha = 0.92f)

    val inputBorder: Color
        @Composable get() = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.28f) else Color.White

    // Semantic Status Badges
    val statusBadgeBg: Color
        @Composable get() = if (isDark) Color(0xFF052E16) else Color(0xFFDCFCE7)

    val statusBadgeText: Color
        @Composable get() = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)

    val warningBadgeBg: Color
        @Composable get() = if (isDark) Color(0xFF35240E) else Color(0xFFFEF3C7)

    val warningBadgeText: Color
        @Composable get() = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)

    val categoryBadgeBg: Color
        @Composable get() = if (isDark) Color(0xFF0C2438) else Color(0xFFE0F2FE)

    val categoryBadgeText: Color
        @Composable get() = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

    // Dialogs / Overlays
    val dialogBackground: Color
        @Composable get() = if (isDark) Color(0xFF0E2235).copy(alpha = 0.96f) else Color.White.copy(alpha = 0.96f)

    val dialogBorder: Color
        @Composable get() = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color.White
}

/**
 * Full-screen water-inspired fluid gradient background container with soft light reflection.
 * Automatically renders night-water in Dark Mode and sunlit water in Normal Mode.
 */
@Composable
fun WaterScreenBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val bgBrush = if (isDark) DarkWaterFluidGradient else WaterFluidGradient
    val refBrush = if (isDark) DarkWaterLightReflection else WaterLightReflection

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgBrush)
    ) {
        // Soft atmospheric radial light reflection at the top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(420.dp)
                .background(refBrush)
        )
        content()
    }
}

/**
 * Standard frosted-glass translucent card with subtle luminous border and soft shadow.
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    alpha: Float = 0.82f,
    borderAlpha: Float = 0.90f,
    elevation: Dp = 4.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val cardColor = if (isDark) Color(0xFF0E2235).copy(alpha = alpha) else Color.White.copy(alpha = alpha)
    val borderColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.22f * borderAlpha) else Color.White.copy(alpha = borderAlpha)
    val shadowColor = if (isDark) Color(0x66000000) else GlassShadowColor

    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier.shadow(
            elevation = elevation,
            shape = shape,
            spotColor = shadowColor,
            ambientColor = shadowColor
        ),
        shape = shape,
        color = cardColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

/**
 * High-opacity milky white (light) or luminous deep navy (dark) focus card for prominent items.
 */
@Composable
fun GlassFocusCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    elevation: Dp = 6.dp,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val isDark = LocalIsDarkTheme.current
    val cardColor = if (isDark) Color(0xFF132B42).copy(alpha = 0.92f) else Color.White.copy(alpha = 0.94f)
    val borderColor = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.38f) else Color.White
    val shadowColor = if (isDark) Color(0x77000000) else GlassShadowColor

    val clickableModifier = if (onClick != null) {
        modifier.clickable { onClick() }
    } else {
        modifier
    }

    Surface(
        modifier = clickableModifier.shadow(
            elevation = elevation,
            shape = shape,
            spotColor = shadowColor,
            ambientColor = shadowColor
        ),
        shape = shape,
        color = cardColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            content = content
        )
    }
}

/**
 * High-contrast rounded pill primary button.
 * Pitch-black in Normal Mode, crisp pure white in Dark Mode.
 */
@Composable
fun BlackPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    isFullWidth: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    val buttonBg = if (!enabled) {
        if (isDark) Color(0xFF334155) else Color(0xFF94A3B8)
    } else {
        if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    }
    val contentColor = if (!enabled) {
        if (isDark) Color(0xFF94A3B8) else Color(0xFFE2E8F0)
    } else {
        if (isDark) Color(0xFF0F172A) else Color.White
    }

    Surface(
        shape = RoundedCornerShape(50),
        color = buttonBg,
        modifier = (if (isFullWidth) modifier.fillMaxWidth() else modifier)
            .shadow(
                if (enabled) 6.dp else 0.dp,
                RoundedCornerShape(50),
                spotColor = if (isDark) Color(0x4038BDF8) else Color(0x330F172A)
            )
            .clickable(enabled = enabled) { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (isFullWidth) 24.dp else 18.dp,
                vertical = if (isFullWidth) 14.dp else 11.dp
            ),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(
                            if (isDark) Color(0xFF0F172A) else Color.White,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDark) Color.White else Color(0xFF0F172A),
                        modifier = Modifier.size(12.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = contentColor
            )
        }
    }
}

/**
 * Reusable frosted-glass text input field with high contrast text.
 */
@Composable
fun GlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    maxLines: Int = 1,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    isError: Boolean = false
) {
    val isDark = LocalIsDarkTheme.current
    val focusedContainer = if (isDark) Color(0xFF132B42).copy(alpha = 0.95f) else Color.White.copy(alpha = 0.94f)
    val unfocusedContainer = if (isDark) Color(0xFF0E2235).copy(alpha = 0.88f) else Color.White.copy(alpha = 0.82f)
    val textColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val placeholderColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF64748B)
    val unfocusedBorder = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.28f) else Color.White

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = placeholderColor,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp)
            )
        },
        leadingIcon = if (leadingIcon != null) {
            {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (isError) Error else Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        trailingIcon = trailingIcon,
        singleLine = singleLine,
        maxLines = maxLines,
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        isError = isError,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = focusedContainer,
            unfocusedContainerColor = unfocusedContainer,
            errorContainerColor = if (isDark) Color(0xFF33141F) else Color.White.copy(alpha = 0.90f),
            focusedTextColor = textColor,
            unfocusedTextColor = textColor,
            focusedBorderColor = Color(0xFF38BDF8),
            unfocusedBorderColor = unfocusedBorder,
            errorBorderColor = Error
        ),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * Transparent Top App Bar styled for the deep oceanic teal top gradient.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GlassTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    subtitle: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    val isDark = LocalIsDarkTheme.current
    TopAppBar(
        title = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.4).sp
                    ),
                    color = Color.White
                )
                if (subtitle != null) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .padding(start = 8.dp, end = 4.dp)
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (isDark) 0.12f else 0.22f))
                        .border(
                            BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = if (isDark) 0.25f else 0.40f)
                            ),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

/**
 * Reusable frosted-glass empty state container.
 */
@Composable
fun GlassEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val circleBg = if (isDark) Color(0xFF10283C) else Color(0xFFE0F2FE)
    val titleColor = if (isDark) Color(0xFFFFFFFF) else Color(0xFF0F172A)
    val subColor = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

    GlassCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        alpha = if (isDark) 0.82f else 0.70f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(circleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = titleColor
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = subColor
            )
        }
    }
}

/**
 * Small, clean, compact two-option theme toggle: [ ☀ Normal | 🌙 Dark ]
 * Side-by-side, compact pill shape, clear highlight, no large cards.
 */
@Composable
fun CompactThemeSwitcher(
    currentMode: AppThemeMode,
    onModeChanged: (AppThemeMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val containerBg = if (isDark) Color(0xFF0A1C2C) else Color(0xFFE2E8F0).copy(alpha = 0.70f)
    val containerBorder = if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.90f)

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(containerBg)
            .border(BorderStroke(1.dp, containerBorder), RoundedCornerShape(20.dp))
            .padding(2.5.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            Pair(AppThemeMode.NORMAL, "Normal"),
            Pair(AppThemeMode.DARK, "Dark")
        ).forEach { (mode, label) ->
            val isSelected = currentMode == mode
            val selectedBg = if (isDark) Color.White else Color(0xFF0F172A)
            val selectedText = if (isDark) Color(0xFF0F172A) else Color.White
            val unselectedText = if (isDark) Color(0xFF94A3B8) else Color(0xFF475569)

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (isSelected) selectedBg else Color.Transparent)
                    .clickable { onModeChanged(mode) }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (mode == AppThemeMode.NORMAL) "☀" else "🌙",
                        fontSize = 12.sp,
                        color = if (isSelected) selectedText else unselectedText
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (isSelected) selectedText else unselectedText
                    )
                }
            }
        }
    }
}
