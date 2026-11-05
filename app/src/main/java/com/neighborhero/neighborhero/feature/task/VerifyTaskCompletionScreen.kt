package com.neighborhero.neighborhero.feature.task

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.data.network.dto.TaskEvidenceDto
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun VerifyTaskCompletionScreen(
    task: HelpRequest? = null,
    evidence: TaskEvidenceDto? = null,
    currentUser: User? = null,
    onBackClick: () -> Unit,
    onVerified: (rating: Int, feedback: String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }
    val tags = listOf("Super fast", "Very polite", "Excellent work", "Punctual")

    val helperName = task?.assignedHelperName ?: "Assigned Helper"
    val taskTitle = task?.title ?: "Community Assistance"

    val isRequester = currentUser == null || (
        (currentUser.id.isNotBlank() && currentUser.id == task?.requesterId) ||
        (currentUser.fullName.isNotBlank() && currentUser.fullName.equals(task?.requesterName, ignoreCase = true))
    )

    WaterScreenBackground {
        if (!isRequester) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = { GlassTopBar(title = "Task Verification", onBackClick = onBackClick) }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    GlassFocusCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.warningBadgeBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = GlassTheme.warningBadgeText,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Requester Verification Only",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Only the requester (${task?.requesterName ?: "requester"}) can review completion evidence, rate the helper, and release community points.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = GlassTheme.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            BlackPillButton(
                                text = "Return to Task",
                                onClick = onBackClick,
                                isFullWidth = true
                            )
                        }
                    }
                }
            }
            return@WaterScreenBackground
        }

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                GlassTopBar(
                    title = "Verify Completion",
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Celebration Badge Banner
                GlassFocusCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(GlassTheme.statusBadgeBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.TaskAlt,
                                contentDescription = null,
                                tint = GlassTheme.statusBadgeText,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            "Task Completed!",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Please review the submitted work for \"$taskTitle\" and thank your helper.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            ),
                            color = GlassTheme.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                // Evidence Section Card
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = GlassTheme.accent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Completed Work Evidence",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                        }
                        Text(
                            "Just now",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = GlassTheme.textMuted
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Evidence Photo Frame
                    if (!evidence?.proofPhotoUrl.isNullOrBlank()) {
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp),
                            color = GlassTheme.chipBackground,
                            border = BorderStroke(1.dp, GlassTheme.chipBorder)
                        ) {
                            NetworkImage(
                                url = evidence?.proofPhotoUrl,
                                modifier = Modifier.fillMaxSize(),
                                contentDescription = "Completed Work Evidence Photo"
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_verify_lawn1),
                                    contentDescription = "Work proof fallback",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    } else {
                        // Fallback Photo Evidence Grid
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(130.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                color = GlassTheme.chipBackground,
                                border = BorderStroke(1.dp, GlassTheme.chipBorder)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_verify_lawn1),
                                    contentDescription = "Work proof 1",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp)),
                                shape = RoundedCornerShape(16.dp),
                                color = GlassTheme.chipBackground,
                                border = BorderStroke(1.dp, GlassTheme.chipBorder)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_verify_lawn2),
                                    contentDescription = "Work proof 2",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }

                    if (!evidence?.notes.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = GlassTheme.cardBackground,
                            border = BorderStroke(1.dp, GlassTheme.cardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Helper Notes: \"${evidence?.notes}\"",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = GlassTheme.textSecondary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Verified Helper Tag Pill
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = GlassTheme.cardBackground,
                        border = BorderStroke(1.dp, GlassTheme.cardBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!task?.assignedHelperAvatarUrl.isNullOrBlank()) {
                                    NetworkImage(
                                        url = task?.assignedHelperAvatarUrl,
                                        modifier = Modifier.fillMaxSize(),
                                        contentDescription = helperName
                                    ) {
                                        Text(
                                            text = helperName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                            color = GlassTheme.accent
                                        )
                                    }
                                } else {
                                    Text(
                                        text = helperName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                        color = GlassTheme.accent
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    helperName,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                                Text(
                                    "Completed via NeighborHero",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = GlassTheme.textMuted
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GlassTheme.statusBadgeBg,
                                border = BorderStroke(1.dp, GlassTheme.statusBadgeText.copy(alpha = 0.35f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = GlassTheme.statusBadgeText,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        "Verified",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = GlassTheme.statusBadgeText
                                    )
                                }
                            }
                        }
                    }
                }

                // Rating & Review Form Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    alpha = 0.88f
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Rate Your Experience",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            "How was your interaction with $helperName?",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = GlassTheme.textMuted
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            (1..5).forEach { i ->
                                IconButton(
                                    onClick = { rating = i },
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = "Star $i",
                                        tint = if (i <= rating) Color(0xFFF59E0B) else GlassTheme.textMuted.copy(alpha = 0.4f),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Feedback Tag Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            tags.take(3).forEach { tag ->
                                val selected = selectedTags.contains(tag)
                                Surface(
                                    shape = RoundedCornerShape(50),
                                    color = if (selected) GlassTheme.chipSelectedBg else GlassTheme.chipBackground,
                                    border = BorderStroke(1.dp, if (selected) GlassTheme.chipSelectedBg else GlassTheme.chipBorder),
                                    modifier = Modifier.clickable {
                                        if (selected) selectedTags.remove(tag) else selectedTags.add(tag)
                                    }
                                ) {
                                    Text(
                                        tag,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        ),
                                        color = if (selected) GlassTheme.chipSelectedText else GlassTheme.chipText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = reviewText,
                            onValueChange = { reviewText = it },
                            placeholder = {
                                Text(
                                    "Write a kind note or feedback (optional)...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = GlassTheme.textMuted
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GlassTheme.cardBackground,
                                unfocusedContainerColor = GlassTheme.cardBackground,
                                focusedTextColor = GlassTheme.textPrimary,
                                unfocusedTextColor = GlassTheme.textPrimary,
                                focusedBorderColor = GlassTheme.accent,
                                unfocusedBorderColor = GlassTheme.cardBorder
                            )
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    BlackPillButton(
                        text = if (isRequester) "Confirm & Release Verification (+150 pts)" else "Requester Only",
                        icon = Icons.Default.CheckCircle,
                        onClick = {
                            if (isRequester) {
                                onVerified(rating, reviewText)
                            }
                        },
                        enabled = isRequester,
                        isFullWidth = true
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun VerifyTaskCompletionScreenPreview() {
    NeighborheroTheme {
        VerifyTaskCompletionScreen(
            task = null,
            onBackClick = {},
            onVerified = { _, _ -> }
        )
    }
}
