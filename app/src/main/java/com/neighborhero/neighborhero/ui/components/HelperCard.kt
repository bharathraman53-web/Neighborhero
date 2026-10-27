package com.neighborhero.neighborhero.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun HelperCard(
    helper: HelperProfile,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(PrimaryFixed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = helper.name.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                        style = MaterialTheme.typography.titleMedium,
                        color = OnPrimaryFixed
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = helper.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = OnSurface
                        )
                        if (helper.isVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Secondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Text(
                        text = "${helper.neighborhood} • ${helper.distance}",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Rating",
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${helper.rating}",
                        style = MaterialTheme.typography.labelLarge,
                        color = OnSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = helper.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                helper.skills.take(3).forEach { skill ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = SecondaryContainer
                    ) {
                        Text(
                            text = skill,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = OnSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}
