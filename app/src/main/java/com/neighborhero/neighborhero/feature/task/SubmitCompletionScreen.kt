package com.neighborhero.neighborhero.feature.task

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.ai.AiMatchStatus
import com.neighborhero.neighborhero.ai.EvidenceAssessmentStatus
import com.neighborhero.neighborhero.ai.EvidenceClassificationResult
import com.neighborhero.neighborhero.ai.ImageEvidenceClassifier
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun SubmitCompletionScreen(
    task: HelpRequest? = null,
    currentUser: User? = null,
    onBackClick: () -> Unit,
    onSubmitSuccess: (notes: String) -> Unit,
    onSubmitSuccessWithPhoto: ((notes: String, photoBytes: ByteArray?, mimeType: String, fileName: String, onDone: (Boolean, String?) -> Unit) -> Unit)? = null
) {
    val isRequester = currentUser != null && (
        (currentUser.id.isNotBlank() && currentUser.id == task?.requesterId) ||
        (currentUser.fullName.isNotBlank() && currentUser.fullName.equals(task?.requesterName, ignoreCase = true))
    )
    val isAssignedHelper = currentUser == null || (
        (currentUser.id.isNotBlank() && currentUser.id == task?.assignedHelperId) ||
        (!task?.assignedHelperName.isNullOrBlank() && currentUser.fullName.equals(task?.assignedHelperName, ignoreCase = true)) ||
        task?.assignedHelperId.isNullOrBlank()
    )
    val isAlreadySubmitted = (task?.status == RequestStatus.PENDING_VERIFICATION || task?.status == RequestStatus.COMPLETED)
    val context = LocalContext.current
    val taskTitle = task?.title ?: "Community Task Assistance"
    var notes by remember { mutableStateOf("") }

    var selectedPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedPhotoMime by remember { mutableStateOf("image/jpeg") }
    var selectedPhotoName by remember { mutableStateOf("evidence.jpg") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                selectedPhotoMime = mime
                selectedPhotoName = when {
                    mime.contains("png", ignoreCase = true) -> "evidence.png"
                    mime.contains("webp", ignoreCase = true) -> "evidence.webp"
                    else -> "evidence.jpg"
                }
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    if (bytes.size > 5 * 1024 * 1024) {
                        errorMessage = "Selected proof photo exceeds 5 MB limit. Please select a smaller photo."
                    } else {
                        selectedPhotoBytes = bytes
                        errorMessage = null
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load image: ${e.message}"
            }
        }
    }

    val previewBitmap = remember(selectedPhotoBytes) {
        selectedPhotoBytes?.let {
            try {
                BitmapFactory.decodeByteArray(it, 0, it.size)?.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }

    val imageClassifier = remember { ImageEvidenceClassifier(context) }
    DisposableEffect(imageClassifier) {
        onDispose {
            imageClassifier.close()
        }
    }

    var aiAssessment by remember { mutableStateOf<EvidenceClassificationResult?>(null) }
    var isAnalyzingImage by remember { mutableStateOf(false) }

    LaunchedEffect(selectedPhotoBytes, task?.category) {
        val bytes = selectedPhotoBytes
        if (bytes != null && bytes.isNotEmpty()) {
            isAnalyzingImage = true
            aiAssessment = imageClassifier.classifyEvidence(bytes, task?.category)
            isAnalyzingImage = false
        } else {
            aiAssessment = null
            isAnalyzingImage = false
        }
    }

    if (isRequester) {
        Scaffold(
            topBar = { AppTopBar(title = "Task Status", onBackClick = onBackClick) }
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
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = Primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Awaiting Helper Completion",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your assigned helper is currently completing this task. Once they submit evidence, you will be notified to review and verify their work.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(
                    onClick = onBackClick
                ) {
                    Text("Back to Active Task", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        return
    }

    if (!isAssignedHelper) {
        Scaffold(
            topBar = { AppTopBar(title = "Restricted Access", onBackClick = onBackClick) }
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
                    text = "Assigned Helper Only",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = OnSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Only the assigned helper (${task?.assignedHelperName ?: "helper"}) can upload evidence and submit completion for this request.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                PrimaryButton(
                    onClick = onBackClick
                ) {
                    Text("Return", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        return
    }

    Scaffold(
        topBar = { AppTopBar(title = "Submit Completion", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (isAlreadySubmitted) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Task completion has already been submitted and is currently pending verification.",
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                if (!errorMessage.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                // Celebration Header Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.TaskAlt, contentDescription = null, tint = Secondary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Almost Done!", style = MaterialTheme.typography.headlineMedium, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("You're submitting completion for $taskTitle.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                    }
                }

                // Points Badge Pill
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = Secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Neighbor Aid Award", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                        }
                        Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) {
                            Text("Earn 150 pts", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = OnSecondaryContainer)
                        }
                    }
                }

                // Completion Notes
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Completion Notes", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text("Required", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    }
                    TextField(
                        value = notes,
                        onValueChange = { if (it.length <= 250) notes = it },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3,
                        enabled = !isSubmitting,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Text("${notes.length}/250", modifier = Modifier.align(Alignment.End), style = MaterialTheme.typography.bodySmall, color = Outline)
                }

                // Proof Photo Upload Box
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Proof Photo", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                        Text(
                            text = if (selectedPhotoBytes != null) "Photo selected" else "Optional",
                            style = MaterialTheme.typography.labelMedium,
                            color = Secondary
                        )
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clickable(enabled = !isSubmitting) {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            if (previewBitmap != null) {
                                Image(
                                    bitmap = previewBitmap,
                                    contentDescription = "Selected proof photo of task",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_proof_garden),
                                    contentDescription = "Proof photo of task",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .clickable(enabled = !isSubmitting) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.65f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (selectedPhotoBytes != null) "Change Photo" else "Select Photo",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    // Local On-Device AI Evidence Relevance Assessment Banner
                    if (isAnalyzingImage) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = Secondary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Analyzing visual evidence locally...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    } else if (aiAssessment != null) {
                        val assessment = aiAssessment!!
                        val matchStatus = assessment.matchStatus
                        val (badgeColor, textColor, icon) = when (matchStatus) {
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
                            colors = CardDefaults.cardColors(containerColor = badgeColor)
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
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = textColor,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = matchStatus.title,
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    )
                                }
                                Text(
                                    text = matchStatus.advisory,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = textColor
                                )
                                Text(
                                    text = "Visual AI assessment is an advisory signal for the requester, never automatic completion proof. The requester makes the final accept/reject decision.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = textColor.copy(alpha = 0.85f)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val canSubmit = !isSubmitting && !isRequester && !isAlreadySubmitted

            PrimaryButton(
                onClick = {
                    if (!canSubmit) return@PrimaryButton
                    if (notes.isBlank()) {
                        errorMessage = "Please enter completion notes describing the finished work."
                        return@PrimaryButton
                    }
                    errorMessage = null
                    isSubmitting = true

                    if (onSubmitSuccessWithPhoto != null) {
                        onSubmitSuccessWithPhoto(
                            notes.trim(),
                            selectedPhotoBytes,
                            selectedPhotoMime,
                            selectedPhotoName
                        ) { success, err ->
                            isSubmitting = false
                            if (!success) {
                                errorMessage = err ?: "Task completion submission failed."
                            }
                        }
                    } else {
                        onSubmitSuccess(notes.trim())
                        isSubmitting = false
                    }
                },
                enabled = canSubmit
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting...", style = MaterialTheme.typography.labelLarge)
                } else if (isRequester) {
                    Text("Requesters Cannot Submit Completion", style = MaterialTheme.typography.labelLarge)
                } else if (isAlreadySubmitted) {
                    Text("Completion Already Submitted", style = MaterialTheme.typography.labelLarge)
                } else {
                    Text("Submit Task Completion", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SubmitCompletionScreenPreview() {
    NeighborheroTheme {
        SubmitCompletionScreen(
            task = null,
            onBackClick = {},
            onSubmitSuccess = {}
        )
    }
}
