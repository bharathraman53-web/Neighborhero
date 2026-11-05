package com.neighborhero.neighborhero.feature.task

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun ConfirmTaskStartScreen(
    task: HelpRequest? = null,
    onBackClick: () -> Unit,
    onStartConfirmed: () -> Unit
) {
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }

    val allChecked = check1 && check2 && check3

    val title = task?.title ?: "Help Request"
    val category = task?.category ?: "Community Help"
    val time = task?.requestedTime ?: "Scheduled Time"
    val requesterName = task?.requesterName ?: "Neighbor"
    val location = task?.location ?: "Community Location"
    val distance = task?.distance ?: "Nearby"

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Confirm Start",
                    onBackClick = onBackClick
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Ready Greeting Banner
                    GlassFocusCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.VerifiedUser,
                                    contentDescription = null,
                                    tint = GlassTheme.accent,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    "Ready to begin?",
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    "Let's ensure everything is set for a safe visit.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }
                    }

                    // Task Summary Card
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        alpha = 0.88f
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GlassTheme.categoryBadgeBg,
                                border = BorderStroke(1.dp, GlassTheme.categoryBadgeText.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    category,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.categoryBadgeText
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "Agreed Time",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = GlassTheme.textMuted
                                )
                                Text(
                                    time,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            title,
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(color = GlassTheme.cardBorder, thickness = 0.8.dp)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val avatarRes = when {
                                    requesterName.contains("Martha", ignoreCase = true) -> R.drawable.img_elderly_martha
                                    requesterName.contains("Marcus", ignoreCase = true) -> R.drawable.img_contact_marcus
                                    requesterName.contains("Sarah", ignoreCase = true) -> R.drawable.img_contact_sarah
                                    requesterName.contains("Eleanor", ignoreCase = true) -> R.drawable.img_eleanor_vance
                                    else -> null
                                }

                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(GlassTheme.accent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!task?.requesterAvatarUrl.isNullOrBlank()) {
                                        NetworkImage(
                                            url = task?.requesterAvatarUrl,
                                            modifier = Modifier.fillMaxSize(),
                                            contentDescription = requesterName
                                        ) {
                                            if (avatarRes != null) {
                                                Image(
                                                    painter = painterResource(id = avatarRes),
                                                    contentDescription = requesterName,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(
                                                    text = requesterName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = GlassTheme.accent
                                                )
                                            }
                                        }
                                    } else if (avatarRes != null) {
                                        Image(
                                            painter = painterResource(id = avatarRes),
                                            contentDescription = requesterName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = requesterName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GlassTheme.accent
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Text(
                                        requesterName,
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GlassTheme.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        "$location ($distance)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                        color = GlassTheme.textMuted
                                    )
                                }
                            }

                            val context = LocalContext.current
                            val phoneToDial = task?.requesterPhone?.takeIf { it.isNotBlank() } ?: "5551234567"
                            Surface(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        try {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToDial"))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    },
                                shape = CircleShape,
                                color = GlassTheme.chipBackground,
                                border = BorderStroke(1.dp, GlassTheme.chipBorder)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Call,
                                        contentDescription = "Call",
                                        tint = GlassTheme.accent,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Pre-Task Safety Checklist
                    Text(
                        "Pre-Task Safety Checklist",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GlassTheme.textPrimary
                    )

                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(22.dp),
                        alpha = 0.88f
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = check1,
                                onCheckedChange = { check1 = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GlassTheme.accent,
                                    uncheckedColor = GlassTheme.textMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Verify Community ID",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Text(
                                    "Ensure your SafeNeighbour badge is visible upon arrival.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }

                        HorizontalDivider(
                            color = GlassTheme.cardBorder,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = check2,
                                onCheckedChange = { check2 = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GlassTheme.accent,
                                    uncheckedColor = GlassTheme.textMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Review Task Instructions",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Text(
                                    "Double-check dietary requirements or drop-off notes.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }

                        HorizontalDivider(
                            color = GlassTheme.cardBorder,
                            thickness = 0.8.dp,
                            modifier = Modifier.padding(vertical = 10.dp)
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = check3,
                                onCheckedChange = { check3 = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = GlassTheme.accent,
                                    uncheckedColor = GlassTheme.textMuted
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Emergency Contact Ready",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Text(
                                    "Confirm support hotline is accessible in case of delays.",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    BlackPillButton(
                        text = "Confirm & Start Task",
                        icon = Icons.Default.PlayArrow,
                        onClick = onStartConfirmed,
                        enabled = allChecked,
                        isFullWidth = true
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConfirmTaskStartScreenPreview() {
    NeighborheroTheme {
        ConfirmTaskStartScreen(
            task = null,
            onBackClick = {},
            onStartConfirmed = {}
        )
    }
}
