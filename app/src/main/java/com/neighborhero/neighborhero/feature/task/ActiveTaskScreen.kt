package com.neighborhero.neighborhero.feature.task

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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

    Scaffold(
        topBar = { AppTopBar(title = "Active Task", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) {
                            Text(
                                text = if (isPendingVerification) "Pending Verification" else "Task In Progress",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = OnSecondaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(title, style = MaterialTheme.typography.headlineMedium, color = OnPrimary)

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = PrimaryFixedDim)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(location, style = MaterialTheme.typography.bodyMedium, color = PrimaryFixedDim)
                        }
                    }
                }

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
                                    .background(PrimaryFixed),
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
                                                style = MaterialTheme.typography.titleMedium,
                                                color = OnPrimaryFixed
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
                                        style = MaterialTheme.typography.titleMedium,
                                        color = OnPrimaryFixed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(partnerName, style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                                Text(partnerRole, style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }

                        val context = LocalContext.current
                        val phoneToDial = task?.requesterPhone?.takeIf { it.isNotBlank() } ?: "5551234567"
                        Row {
                            IconButton(onClick = onChatClick) {
                                Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = "Chat", tint = Primary)
                            }
                            IconButton(
                                onClick = {
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneToDial"))
                                        context.startActivity(intent)
                                    } catch (_: Exception) {}
                                }
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = Primary)
                            }
                        }
                    }
                }
            }

            if (isRequester) {
                if (isPendingVerification) {
                    PrimaryButton(onClick = onVerifyCompletionClick) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Review & Verify Completion", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = SurfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Your helper is currently completing this task. Once they submit evidence, you can verify completion and release points here.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = OnSurfaceVariant
                            )
                        }
                    }
                }
            } else if (isAssignedHelper) {
                if (isPendingVerification) {
                    PrimaryButton(onClick = {}, enabled = false) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Completion Submitted — Awaiting Review", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    PrimaryButton(onClick = onSubmitCompletionClick) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit Task Completion", style = MaterialTheme.typography.labelLarge)
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SurfaceContainerLow,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "This task is currently active between ${task?.requesterName ?: "requester"} and ${task?.assignedHelperName ?: "assigned helper"}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
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
