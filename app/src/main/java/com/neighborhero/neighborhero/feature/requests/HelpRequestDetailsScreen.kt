package com.neighborhero.neighborhero.feature.requests

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import com.neighborhero.neighborhero.ui.components.GlassTheme

@Composable
fun HelpRequestDetailsScreen(
    request: HelpRequest? = null,
    currentUser: User? = null,
    onBackClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onChatClick: () -> Unit,
    onFindHelpersClick: ((String) -> Unit)? = null
) {
    if (request == null) {
        WaterScreenBackground {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = { GlassTopBar(title = "Help Request Details", onBackClick = onBackClick) }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GlassTheme.textMuted, modifier = Modifier.size(36.dp))
                            Text("Request Not Found", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GlassTheme.textPrimary)
                            Text(
                                "This request may have been completed, cancelled, or is no longer available.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = GlassTheme.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            BlackPillButton(text = "Back to Dashboard", onClick = onBackClick)
                        }
                    }
                }
            }
        }
        return
    }

    val isRequester = currentUser != null && (
        (currentUser.id.isNotBlank() && currentUser.id == request.requesterId) ||
        (currentUser.fullName.isNotBlank() && currentUser.fullName.equals(request.requesterName, ignoreCase = true))
    )
    val isAssignedHelper = currentUser != null && (
        (currentUser.id.isNotBlank() && currentUser.id == request.assignedHelperId) ||
        (!request.assignedHelperName.isNullOrBlank() && currentUser.fullName.equals(request.assignedHelperName, ignoreCase = true))
    )

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GlassTopBar(title = "Help Request Details", onBackClick = onBackClick) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                if (isRequester) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GlassTheme.statusBadgeBg,
                        border = BorderStroke(1.dp, GlassTheme.statusBadgeText.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = GlassTheme.statusBadgeText, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "You posted this request. You can track progress and verify completion once finished.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp, fontWeight = FontWeight.Medium),
                                color = GlassTheme.statusBadgeText
                            )
                        }
                    }
                }

                // Status & Urgency Header Frosted Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    alpha = 0.82f,
                    elevation = 6.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GlassTheme.categoryBadgeBg,
                                border = BorderStroke(1.dp, GlassTheme.categoryBadgeText.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = GlassTheme.categoryBadgeText, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = request.category,
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                        color = GlassTheme.categoryBadgeText
                                    )
                                }
                            }

                            if (request.isUrgent) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = GlassTheme.warningBadgeBg,
                                    border = BorderStroke(1.dp, GlassTheme.warningBadgeText.copy(alpha = 0.35f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Error, contentDescription = null, tint = GlassTheme.warningBadgeText, modifier = Modifier.size(13.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Urgent", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp, fontWeight = FontWeight.Bold), color = GlassTheme.warningBadgeText)
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = request.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 21.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.4).sp
                            ),
                            color = GlassTheme.textPrimary
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = GlassTheme.accent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (request.distance.isNotBlank()) "${request.location} (${request.distance})" else request.location,
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = GlassTheme.textSecondary
                            )
                        }
                    }
                }

                // Time Sensitive Callout Frosted Card
                GlassFocusCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(GlassTheme.accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = GlassTheme.accent, modifier = Modifier.size(20.dp))
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                "REQUESTED TIME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textMuted
                            )
                            Text(
                                text = request.requestedTime,
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                        }
                    }
                }

                // Requester Profile Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    alpha = 0.82f
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val avatarRes = when {
                                request.requesterName.contains("Martha", ignoreCase = true) -> R.drawable.img_elderly_martha
                                request.requesterName.contains("Marcus", ignoreCase = true) -> R.drawable.img_contact_marcus
                                request.requesterName.contains("Sarah", ignoreCase = true) -> R.drawable.img_contact_sarah
                                request.requesterName.contains("Eleanor", ignoreCase = true) -> R.drawable.img_eleanor_vance
                                else -> null
                            }

                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, GlassTheme.cardBorder, CircleShape)
                                    .background(GlassTheme.chipBackground),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!request.requesterAvatarUrl.isNullOrBlank()) {
                                    NetworkImage(
                                        url = request.requesterAvatarUrl,
                                        modifier = Modifier.fillMaxSize(),
                                        contentDescription = request.requesterName
                                    ) {
                                        Text(
                                            text = request.requesterName.take(1).uppercase().ifEmpty { "R" },
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = GlassTheme.textPrimary
                                        )
                                    }
                                } else if (avatarRes != null) {
                                    Image(
                                        painter = painterResource(id = avatarRes),
                                        contentDescription = request.requesterName,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = request.requesterName.take(1).uppercase().ifEmpty { "R" },
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = GlassTheme.textPrimary
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = request.requesterName,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GlassTheme.textPrimary
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = GlassTheme.accent, modifier = Modifier.size(15.dp))
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "${request.requesterRating} (${request.requesterTaskCount} community tasks)",
                                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                                        color = GlassTheme.textSecondary
                                    )
                                }
                            }
                        }

                        IconButton(
                            onClick = onChatClick,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(GlassTheme.cardBackground)
                                .border(BorderStroke(1.dp, GlassTheme.cardBorder), CircleShape)
                        ) {
                            Icon(Icons.Default.ChatBubble, contentDescription = "Chat", tint = GlassTheme.textPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Request Details Box
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    alpha = 0.82f
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "Request Details",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = request.description,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp, lineHeight = 20.sp),
                            color = GlassTheme.textSecondary
                        )
                    }
                }

                val buttonEnabled: Boolean
                val buttonText: String
                val buttonAction: () -> Unit

                if (isRequester) {
                    when (request.status) {
                        RequestStatus.OPEN -> {
                            buttonEnabled = true
                            buttonText = "View AI Recommended Helpers"
                            buttonAction = {
                                if (onFindHelpersClick != null) {
                                    onFindHelpersClick(request.id)
                                } else {
                                    onAcceptClick()
                                }
                            }
                        }
                        RequestStatus.IN_PROGRESS -> {
                            buttonEnabled = true
                            buttonText = "Helper Working on Task — View Progress"
                            buttonAction = onAcceptClick
                        }
                        RequestStatus.PENDING_VERIFICATION -> {
                            buttonEnabled = true
                            buttonText = "Review & Verify Completion"
                            buttonAction = onAcceptClick
                        }
                        RequestStatus.COMPLETED -> {
                            buttonEnabled = false
                            buttonText = "Task Completed"
                            buttonAction = {}
                        }
                        RequestStatus.CANCELLED -> {
                            buttonEnabled = false
                            buttonText = "Request Cancelled"
                            buttonAction = {}
                        }
                    }
                } else {
                    when (request.status) {
                        RequestStatus.OPEN -> {
                            buttonEnabled = true
                            buttonText = "Offer Help / Accept Request"
                            buttonAction = onAcceptClick
                        }
                        RequestStatus.IN_PROGRESS -> {
                            if (isAssignedHelper) {
                                buttonEnabled = true
                                buttonText = "Continue Active Task"
                                buttonAction = onAcceptClick
                            } else {
                                buttonEnabled = false
                                buttonText = "In Progress (Another helper assigned)"
                                buttonAction = {}
                            }
                        }
                        RequestStatus.PENDING_VERIFICATION -> {
                            if (isAssignedHelper) {
                                buttonEnabled = true
                                buttonText = "Completion Submitted (Pending Review)"
                                buttonAction = onAcceptClick
                            } else {
                                buttonEnabled = false
                                buttonText = "Pending Requester Verification"
                                buttonAction = {}
                            }
                        }
                        RequestStatus.COMPLETED -> {
                            buttonEnabled = false
                            buttonText = "Task Completed"
                            buttonAction = {}
                        }
                        RequestStatus.CANCELLED -> {
                            buttonEnabled = false
                            buttonText = "Request Cancelled"
                            buttonAction = {}
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Solid Pitch-Black Primary Pill Action Button
                BlackPillButton(
                    text = buttonText,
                    onClick = buttonAction,
                    enabled = buttonEnabled,
                    isFullWidth = true,
                    icon = Icons.Default.Handshake
                )

                Spacer(modifier = Modifier.height(28.dp))
            }
        }
    }
}
