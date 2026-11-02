package com.neighborhero.neighborhero.feature.requests

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun HelpRequestDetailsScreen(
    request: HelpRequest? = null,
    currentUser: User? = null,
    onBackClick: () -> Unit,
    onAcceptClick: () -> Unit,
    onChatClick: () -> Unit
) {
    if (request == null) {
        Scaffold(
            topBar = { AppTopBar(title = "Help Request Details", onBackClick = onBackClick) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Outline, modifier = Modifier.size(36.dp))
                        Text("Request Not Found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = OnSurface)
                        Text("This request may have been completed, cancelled, or is no longer available.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                        PrimaryButton(onClick = onBackClick) {
                            Text("Back to Dashboard")
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
    Scaffold(
        topBar = { AppTopBar(title = "Help Request Details", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isRequester) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryFixed,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = OnPrimaryFixed, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "You posted this request. You can track progress and verify completion once finished.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnPrimaryFixed
                        )
                    }
                }
            }

            // Status & Urgency Header Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) {
                            Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ShoppingBasket, contentDescription = null, tint = OnSecondaryContainer, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(request.category, style = MaterialTheme.typography.labelMedium, color = OnSecondaryContainer)
                            }
                        }

                        if (request.isUrgent) {
                            Surface(shape = RoundedCornerShape(12.dp), color = ErrorContainer) {
                                Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Error, contentDescription = null, tint = OnErrorContainer, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Urgent", style = MaterialTheme.typography.labelMedium, color = OnErrorContainer)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(request.title, style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Secondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${request.location} (${request.distance})", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                }
            }

            // Time Sensitive Callout Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryFixed)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Schedule, contentDescription = null, tint = Primary)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text("REQUESTED TIME", style = MaterialTheme.typography.labelSmall, color = OnPrimaryFixedVariant)
                        Text(request.requestedTime, style = MaterialTheme.typography.headlineSmall, color = OnPrimaryFixed, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Requester Profile Card with dynamic image resolution
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                                .background(SecondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!request.requesterAvatarUrl.isNullOrBlank()) {
                                NetworkImage(
                                    url = request.requesterAvatarUrl,
                                    modifier = Modifier.fillMaxSize(),
                                    contentDescription = request.requesterName
                                ) {
                                    if (avatarRes != null) {
                                        Image(
                                            painter = painterResource(id = avatarRes),
                                            contentDescription = request.requesterName,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Text(
                                            text = request.requesterName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                            style = MaterialTheme.typography.labelLarge,
                                            color = OnSecondaryContainer,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
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
                                    text = request.requesterName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = OnSecondaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(request.requesterName, style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Secondary, modifier = Modifier.size(16.dp))
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB800), modifier = Modifier.size(14.dp))
                                Text(" ${request.requesterRating} (${request.requesterTaskCount} community tasks)", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                    }

                    IconButton(onClick = onChatClick) {
                        Icon(Icons.Default.ChatBubble, contentDescription = "Chat", tint = OnSurfaceVariant)
                    }
                }
            }

            // Request Details Box
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Request Details", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(request.description, style = MaterialTheme.typography.bodyLarge, color = OnSurfaceVariant)
                }
            }

            val buttonEnabled: Boolean
            val buttonText: String
            val buttonAction: () -> Unit

            if (isRequester) {
                when (request.status) {
                    RequestStatus.OPEN -> {
                        buttonEnabled = false
                        buttonText = "Waiting for Helper to Offer Assistance"
                        buttonAction = {}
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

            PrimaryButton(onClick = buttonAction, enabled = buttonEnabled) {
                Icon(Icons.Default.Handshake, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonText, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun HelpRequestDetailsScreenPreview() {
    val sampleRequest = HelpRequest(
        id = "req_1",
        title = "Weekly Groceries Pickup",
        category = "Groceries",
        description = "Recovering from knee surgery and unable to drive. Need someone to pick up pre-paid groceries from Maplewood Co-op.",
        requesterName = "Martha W.",
        requesterRating = 4.9f,
        requesterTaskCount = 42,
        location = "Maplewood District",
        distance = "0.3 mi away",
        requestedTime = "Due Today, 4:00 PM",
        isUrgent = true,
        status = RequestStatus.OPEN
    )
    NeighborheroTheme {
        HelpRequestDetailsScreen(
            request = sampleRequest,
            onBackClick = {},
            onAcceptClick = {},
            onChatClick = {}
        )
    }
}
