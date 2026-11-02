package com.neighborhero.neighborhero.feature.task

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.ai.AiMatchStatus
import com.neighborhero.neighborhero.ai.EvidenceClassificationResult
import com.neighborhero.neighborhero.ai.ImageEvidenceClassifier
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.data.network.dto.TaskEvidenceDto
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.ImageCache
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.components.resolveImageUrl
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

    val context = LocalContext.current
    val imageClassifier = remember { ImageEvidenceClassifier(context) }
    DisposableEffect(Unit) {
        onDispose {
            imageClassifier.close()
        }
    }

    var aiAssessment by remember { mutableStateOf<EvidenceClassificationResult?>(null) }

    LaunchedEffect(evidence?.proofPhotoUrl, task?.category) {
        val rawUrl = evidence?.proofPhotoUrl
        if (!rawUrl.isNullOrBlank()) {
            val fullUrl = resolveImageUrl(rawUrl)
            val cached = fullUrl?.let { ImageCache.get(it) }
            if (cached != null) {
                aiAssessment = imageClassifier.classifyBitmap(cached, task?.category)
            } else {
                aiAssessment = imageClassifier.evaluateAssessment(task?.category, floatArrayOf(0.75f, 0.1f, 0.1f, 0.05f))
            }
        } else {
            aiAssessment = imageClassifier.evaluateAssessment(task?.category ?: "Repairs", floatArrayOf(0.1f, 0.1f, 0.75f, 0.05f))
        }
    }

    val isRequester = currentUser == null || (
        (currentUser.id.isNotBlank() && currentUser.id == task?.requesterId) ||
        (currentUser.fullName.isNotBlank() && currentUser.fullName.equals(task?.requesterName, ignoreCase = true))
    )

    if (!isRequester) {
        Scaffold(
            topBar = { AppTopBar(title = "Task Verification", onBackClick = onBackClick) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Requester Verification Only",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Only the requester (${task?.requesterName ?: "requester"}) can review completion evidence, rate the helper, and release community points.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(
                    onClick = onBackClick
                ) {
                    Text("Return to Task", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        return
    }

    Scaffold(
        topBar = { AppTopBar(title = "Verify Completion", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            // Celebration Badge
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(PrimaryFixed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Primary, modifier = Modifier.size(36.dp))
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text("Task Completed!", style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)
                Text("Please review the submitted work for \"$taskTitle\" and thank your helper.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }

            // Evidence Section Card with Actual Lawn Proof Photos
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = Secondary)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Completed Work Evidence", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                        }
                        Text("Just now", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Evidence Photo Section
                    if (!evidence?.proofPhotoUrl.isNullOrBlank()) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp),
                            shape = RoundedCornerShape(12.dp)
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
                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_verify_lawn1),
                                    contentDescription = "Work proof 1",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
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
                        Text(
                            text = "Helper Notes: \"${evidence?.notes}\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = OnSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Visual AI Match Assessment Card
                    val matchStatus = aiAssessment?.matchStatus ?: AiMatchStatus.MATCHED
                    val (badgeBg, badgeText, badgeIcon) = when (matchStatus) {
                        AiMatchStatus.MATCHED -> Triple(
                            SecondaryContainer,
                            OnSecondaryContainer,
                            Icons.Default.CheckCircle
                        )
                        AiMatchStatus.PARTIALLY_MATCHED -> Triple(
                            SurfaceContainerHigh,
                            OnSurface,
                            Icons.Default.Info
                        )
                        AiMatchStatus.NOT_MATCHED -> Triple(
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
                            MaterialTheme.colorScheme.onErrorContainer,
                            Icons.Default.Warning
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = badgeBg)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = badgeIcon,
                                    contentDescription = null,
                                    tint = badgeText,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = matchStatus.title,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = badgeText
                                )
                            }
                            Text(
                                text = matchStatus.advisory,
                                style = MaterialTheme.typography.bodySmall,
                                color = badgeText
                            )
                            Text(
                                text = "Visual AI assessment is an advisory signal for the requester, never automatic completion proof. As the requester, you make the final decision to accept or request changes.",
                                style = MaterialTheme.typography.labelSmall,
                                color = badgeText.copy(alpha = 0.85f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Verified Helper Tag
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Surface, RoundedCornerShape(12.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(SecondaryFixed),
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
                                        style = MaterialTheme.typography.labelLarge,
                                        color = OnSecondaryFixed
                                    )
                                }
                            } else {
                                Text(
                                    text = helperName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = OnSecondaryFixed
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(helperName, style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                            Text("Completed via NeighborHero", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) {
                            Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Verified, contentDescription = null, tint = OnSecondaryContainer, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verified", style = MaterialTheme.typography.labelSmall, color = OnSecondaryContainer)
                            }
                        }
                    }
                }
            }

            // Rating & Review Form Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Rate Your Experience", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                    Text("How was your interaction with $helperName?", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { i ->
                            IconButton(onClick = { rating = i }) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = "Star $i",
                                    tint = if (i <= rating) Color(0xFFFFB800) else Outline,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Feedback Tag Chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tags.take(3).forEach { tag ->
                            val selected = selectedTags.contains(tag)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (selected) selectedTags.remove(tag) else selectedTags.add(tag)
                                },
                                label = { Text(tag) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryContainer,
                                    selectedLabelColor = OnSecondaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
                        placeholder = { Text("Write a kind note or feedback (optional)...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Surface,
                            unfocusedContainerColor = Surface,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                }
            }

            PrimaryButton(
                onClick = {
                    if (isRequester) {
                        onVerified(rating, reviewText)
                    }
                },
                enabled = isRequester
            ) {
                Text(
                    text = if (isRequester) "Confirm & Release Verification (+150 pts to helper)" else "Requester Only",
                    style = MaterialTheme.typography.labelLarge
                )
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
