package com.neighborhero.neighborhero.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.R
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

    val categoryIcon = when (request.category.lowercase().trim()) {
        "groceries" -> Icons.Default.ShoppingCart
        "books" -> Icons.AutoMirrored.Filled.MenuBook
        "repairs" -> Icons.Default.Build
        "pet care", "pet help" -> Icons.Default.Pets
        "heavy lifting" -> Icons.Default.FitnessCenter
        "gardening", "yard work" -> Icons.Default.Yard
        "tech support" -> Icons.Default.Computer
        else -> Icons.Default.Handshake
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isDark) 3.dp else 4.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = if (isDark) Color(0x66000000) else GlassShadowColor,
                ambientColor = if (isDark) Color(0x66000000) else GlassShadowColor
            )
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = GlassTheme.cardBackground,
        border = BorderStroke(
            1.dp,
            if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Category, Title, Description, Location & Time
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                // Header: Category Pill & Urgency Badge
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = categoryBg,
                        border = BorderStroke(1.dp, Color(0x330284C7))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon,
                                contentDescription = null,
                                tint = categoryText,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = request.category,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = categoryText
                            )
                        }
                    }

                    if (request.isUrgent) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isDark) Color(0xFF3F1D1D) else Color(0xFFFEE2E2),
                            border = BorderStroke(
                                1.dp,
                                if (isDark) Color(0x40EF4444) else Color(0x40EF4444)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color(0xFFEF4444),
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = "URGENT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = Color(0xFFEF4444)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = request.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = GlassTheme.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = request.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    ),
                    color = GlassTheme.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Footer: Location & Time in a unified single row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    val locationDisplay = if (request.distance.isNotBlank() && !request.distance.contains("away")) {
                        request.distance
                    } else if (request.distance.isNotBlank()) {
                        "${request.location} (${request.distance})"
                    } else {
                        request.location
                    }
                    Text(
                        text = locationDisplay,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        color = GlassTheme.textMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    if (request.requestedTime.isNotBlank()) {
                        Text(
                            text = " | ",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = GlassTheme.textMuted.copy(alpha = 0.5f)
                        )
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = GlassTheme.textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = request.requestedTime,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                            color = GlassTheme.textMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Right Thumbnail Photo matching category exactly
            val categoryDrawableRes = when (request.category.lowercase().trim()) {
                "groceries" -> R.drawable.img_thumb_supermarket
                "books", "tutoring" -> R.drawable.img_thumb_books
                "repairs", "gardening", "yard work" -> R.drawable.img_proof_garden
                else -> R.drawable.img_thumb_groceries
            }

            Surface(
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(
                    1.dp,
                    if (isDark) Color(0xFF38BDF8).copy(alpha = 0.25f) else Color.White.copy(alpha = 0.85f)
                ),
                shadowElevation = 2.dp,
                modifier = Modifier.size(64.dp)
            ) {
                Image(
                    painter = painterResource(id = categoryDrawableRes),
                    contentDescription = request.category,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
