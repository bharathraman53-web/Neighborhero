package com.neighborhero.neighborhero.feature.task

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun ActiveTaskScreen(
    task: HelpRequest? = null,
    currentUser: User? = null,
    onBackClick: () -> Unit,
    onChatClick: () -> Unit,
    onSubmitCompletionClick: () -> Unit,
    onVerifyCompletionClick: () -> Unit = {}
) {
    val title = task?.title ?: "Active Task"
    val location = task?.location ?: "Community Location"
    val requesterName = task?.requesterName ?: "Neighbor"

    val isRequester = currentUser != null && (
        (currentUser.id.isNotBlank() && currentUser.id == task?.requesterId) ||
        (currentUser.fullName.isNotBlank() && currentUser.fullName.equals(task?.requesterName, ignoreCase = true))
    )
    val isAssignedHelper = currentUser != null && (
        (currentUser.id.isNotBlank() && currentUser.id == task?.assignedHelperId) ||
        (!task?.assignedHelperName.isNullOrBlank() && currentUser.fullName.equals(task?.assignedHelperName, ignoreCase = true))
    )
    val isPendingVerification = (task?.status == RequestStatus.PENDING_VERIFICATION)

    val partnerName = if (isRequester) {
        task?.assignedHelperName?.takeIf { it.isNotBlank() } ?: "Assigned Helper"
    } else {
        requesterName
    }
    val partnerRole = if (isRequester) "Helper Neighbor" else "Requester Neighbor"
    val partnerAvatarUrl = if (isRequester) task?.assignedHelperAvatarUrl else task?.requesterAvatarUrl

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Active Task",
                    onBackClick = onBackClick
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Task Overview Card
                    GlassFocusCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = if (isPendingVerification) GlassTheme.warningBadgeBg else GlassTheme.statusBadgeBg,
                            border = BorderStroke(1.dp, if (isPendingVerification) GlassTheme.warningBadgeText.copy(alpha = 0.35f) else GlassTheme.statusBadgeText.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = if (isPendingVerification) "Pending Verification" else "Task In Progress",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                ),
                                color = if (isPendingVerification) GlassTheme.warningBadgeText else GlassTheme.statusBadgeText
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.4).sp
                            ),
                            color = GlassTheme.textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = GlassTheme.accent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                location,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = GlassTheme.textSecondary
                            )
                        }
                    }

                    // Partner Connection Card
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
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val avatarRes = when {
                                    partnerName.contains("Martha", ignoreCase = true) -> R.drawable.img_elderly_martha
                                    partnerName.contains("Marcus", ignoreCase = true) -> R.drawable.img_contact_marcus
                                    partnerName.contains("Sarah", ignoreCase = true) -> R.drawable.img_contact_sarah
                                    partnerName.contains("Eleanor", ignoreCase = true) -> R.drawable.img_eleanor_vance
                                    else -> null
                                }

                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(GlassTheme.accent.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (!partnerAvatarUrl.isNullOrBlank()) {
                                        NetworkImage(
                                            url = partnerAvatarUrl,
                                            modifier = Modifier.fillMaxSize(),
                                            contentDescription = partnerName
                                        ) {
                                            if (avatarRes != null) {
                                                Image(
                                                    painter = painterResource(id = avatarRes),
                                                    contentDescription = partnerName,
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Text(
                                                    text = partnerName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                    color = GlassTheme.accent
                                                )
                                            }
                                        }
                                    } else if (avatarRes != null) {
                                        Image(
                                            painter = painterResource(id = avatarRes),
                                            contentDescription = partnerName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = partnerName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GlassTheme.accent
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text(
                                        partnerName,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GlassTheme.textPrimary
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        partnerRole,
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = GlassTheme.textMuted
                                    )
                                }
                            }

                            val context = LocalContext.current
                            val phoneToDial = task?.requesterPhone?.takeIf { it.isNotBlank() } ?: "5551234567"
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .clickable { onChatClick() },
                                    shape = CircleShape,
                                    color = GlassTheme.chipBackground,
                                    border = BorderStroke(1.dp, GlassTheme.chipBorder)
                                ) {
                                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.Chat,
                                            contentDescription = "Chat",
                                            tint = GlassTheme.textPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Surface(
                                    modifier = Modifier
                                        .size(40.dp)
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
                    }
                }

                // Bottom Action Buttons / Information Containers
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    if (isRequester) {
                        if (isPendingVerification) {
                            BlackPillButton(
                                text = "Review & Verify Completion",
                                icon = Icons.Default.CheckCircle,
                                onClick = onVerifyCompletionClick,
                                isFullWidth = true
                            )
                        } else {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                alpha = 0.88f
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = GlassTheme.accent,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Your helper is currently completing this task. Once they submit evidence, you can verify completion and release points here.",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            lineHeight = 18.sp
                                        ),
                                        color = GlassTheme.textSecondary
                                    )
                                }
                            }
                        }
                    } else if (isAssignedHelper) {
                        if (isPendingVerification) {
                            GlassCard(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(20.dp),
                                alpha = 0.88f
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = GlassTheme.statusBadgeText,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Completion Submitted — Awaiting Requester Review",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = GlassTheme.statusBadgeText
                                    )
                                }
                            }
                        } else {
                            BlackPillButton(
                                text = "Submit Task Completion",
                                icon = Icons.Default.CheckCircle,
                                onClick = onSubmitCompletionClick,
                                isFullWidth = true
                            )
                        }
                    } else {
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            alpha = 0.88f
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = GlassTheme.accent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "This task is currently active between ${task?.requesterName ?: "requester"} and ${task?.assignedHelperName ?: "assigned helper"}.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    ),
                                    color = GlassTheme.textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ActiveTaskScreenPreview() {
    NeighborheroTheme {
        ActiveTaskScreen(
            task = null,
            onBackClick = {},
            onChatClick = {},
            onSubmitCompletionClick = {}
        )
    }
}
