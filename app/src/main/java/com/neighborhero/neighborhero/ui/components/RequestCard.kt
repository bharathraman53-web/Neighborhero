package com.neighborhero.neighborhero.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
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
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun RequestCard(
    request: HelpRequest,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = LocalIsDarkTheme.current
    val categoryBg = if (isDark) Color(0xFF0C2438) else Color(0xFFE0F2FE)
    val categoryText = if (isDark) Color(0xFF38BDF8) else Color(0xFF0284C7)

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
            // Header: Category Pill & Urgency Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = categoryBg,
                    border = BorderStroke(1.dp, Color(0x330284C7))
                ) {
                    Text(
                        text = request.category,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = categoryText
                    )
                }

                if (request.isUrgent) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDark) Color(0xFF35240E) else Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0x40D97706))
                    ) {
                        Text(
                            text = "⚡ URGENT",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (isDark) Color(0xFFFBBF24) else Color(0xFFB45309)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = request.title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = GlassTheme.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = request.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                ),
                color = GlassTheme.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Footer: Distance, Time, and Circular Action Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${request.location} (${request.distance})",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = GlassTheme.textMuted
                        )
                    }

                    if (request.requestedTime.isNotBlank()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = GlassTheme.textMuted,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = request.requestedTime,
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = GlassTheme.textMuted
                            )
                        }
                    }
                }

                // Circular High-Contrast Button
                Surface(
                    shape = CircleShape,
                    color = GlassTheme.pillButtonBg,
                    modifier = Modifier
                        .size(38.dp)
                        .clickable { onClick() }
                ) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "View Request Details",
                            tint = GlassTheme.pillButtonText,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }
        }
    }
}
