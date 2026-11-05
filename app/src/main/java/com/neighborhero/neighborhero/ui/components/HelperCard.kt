package com.neighborhero.neighborhero.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun HelperCard(
    helper: HelperProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val compatBg = if (isDark) Color(0xFF042F2E) else Color(0xFFCCFBF1)
    val compatText = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F766E)
    val availBg = if (isDark) Color(0xFF052E16) else Color(0xFFDCFCE7)
    val availText = if (isDark) Color(0xFF4ADE80) else Color(0xFF15803D)
    val explainBg = if (isDark) Color(0xFF0A1C2C).copy(alpha = 0.70f) else Color.White.copy(alpha = 0.60f)

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 3.dp else 5.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isDark) Color(0x66000000) else GlassShadowColor,
                ambientColor = if (isDark) Color(0x66000000) else GlassShadowColor
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = GlassTheme.cardBackground,
        border = BorderStroke(1.dp, GlassTheme.cardBorder)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // AI Compatibility Badge Bar
            if (helper.matchPercentage > 0) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = compatBg,
                        border = BorderStroke(1.dp, compatText.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = compatText,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "${helper.matchPercentage}% AI Compatibility",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = compatText
                            )
                        }
                    }

                    if (helper.isAvailableToday) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = availBg,
                            border = BorderStroke(1.dp, availText.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(availText, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Available Today",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = availText
                                )
                            }
                        }
                    }
                }
            }

            // Profile Row: Avatar, Name, Location, Rating
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF132B42) else Color(0xFFE2E8F0))
                        .border(
                            BorderStroke(
                                1.5.dp,
                                if (isDark) Color(0xFF38BDF8).copy(alpha = 0.35f) else Color.White
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (helper.avatarUrl.isNotBlank()) {
                        NetworkImage(
                            url = helper.avatarUrl,
                            modifier = Modifier.fillMaxSize(),
                            contentDescription = "${helper.name} avatar"
                        ) {
                            Text(
                                text = helper.name.take(1).uppercase().ifEmpty { "H" },
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A)
                            )
                        }
                    } else {
                        Text(
                            text = helper.name.take(1).uppercase().ifEmpty { "H" },
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (isDark) Color(0xFF38BDF8) else Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = helper.name,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        if (helper.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Helper",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${helper.neighborhood} • ${helper.distance}",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = GlassTheme.textMuted
                        )
                    }
                }

                // Rating Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isDark) Color(0xFF35240E) else Color(0xFFFFFBEB),
                    border = BorderStroke(
                        1.dp,
                        if (isDark) Color(0x66F59E0B) else Color(0x40F59E0B)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format("%.1f", helper.rating),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isDark) Color(0xFFFDE68A) else Color(0xFFB45309)
                        )
                    }
                }
            }

            // AI Explainability Reasons (Glass Stack)
            if (helper.matchingReasons.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = explainBg,
                    border = BorderStroke(1.dp, if (isDark) Color(0x2638BDF8) else Color(0x120F172A)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        for (reason in helper.matchingReasons.take(2)) {
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = if (isDark) Color(0xFF2DD4BF) else Color(0xFF0F766E),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = reason,
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    color = GlassTheme.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Skills and Connect Button Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    helper.skills.take(2).forEach { skill ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = GlassTheme.chipBackground,
                            border = BorderStroke(1.dp, GlassTheme.chipBorder)
                        ) {
                            Text(
                                text = skill,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = GlassTheme.chipText
                            )
                        }
                    }
                }

                // Rounded Pill Action Button
                Surface(
                    shape = RoundedCornerShape(50),
                    color = GlassTheme.pillButtonBg,
                    modifier = Modifier.clickable { onClick() }
                ) {
                    Text(
                        text = "+ Request",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GlassTheme.pillButtonText
                    )
                }
            }
        }
    }
}
