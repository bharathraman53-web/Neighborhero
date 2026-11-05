package com.neighborhero.neighborhero.feature.task

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
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
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
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

    WaterScreenBackground {
        if (isRequester) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = { GlassTopBar(title = "Task Status", onBackClick = onBackClick) }
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
                                    .background(GlassTheme.accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = null,
                                    tint = GlassTheme.accent,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Awaiting Helper Completion",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Your assigned helper is currently completing this task. Once they submit evidence, you will be notified to review and verify their work.",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp
                                ),
                                color = GlassTheme.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            BlackPillButton(
                                text = "Back to Active Task",
                                onClick = onBackClick,
                                isFullWidth = true
                            )
                        }
                    }
                }
            }
            return@WaterScreenBackground
        }

        if (!isAssignedHelper) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = { GlassTopBar(title = "Restricted Access", onBackClick = onBackClick) }
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
                                text = "Assigned Helper Only",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Only the assigned helper (${task?.assignedHelperName ?: "helper"}) can upload evidence and submit completion for this request.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = GlassTheme.textSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(20.dp))
                            BlackPillButton(
                                text = "Return",
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
                    title = "Submit Completion",
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
                    if (isAlreadySubmitted) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GlassTheme.statusBadgeBg,
                            border = BorderStroke(1.dp, GlassTheme.statusBadgeText.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = GlassTheme.statusBadgeText)
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Task completion has already been submitted and is currently pending verification.",
                                    color = GlassTheme.statusBadgeText,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                )
                            }
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GlassTheme.warningBadgeBg,
                            border = BorderStroke(1.dp, GlassTheme.warningBadgeText.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = GlassTheme.warningBadgeText,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(14.dp)
                            )
                        }
                    }

                    // Celebration Header Banner
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
                                "Almost Done!",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "You're submitting completion for $taskTitle.",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = GlassTheme.textSecondary
                            )
                        }
                    }

                    // Points Badge Pill
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        alpha = 0.85f
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = GlassTheme.statusBadgeText,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Neighbor Aid Award",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = GlassTheme.statusBadgeBg,
                                border = BorderStroke(1.dp, GlassTheme.statusBadgeText.copy(alpha = 0.35f))
                            ) {
                                Text(
                                    "Earn 150 pts",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    ),
                                    color = GlassTheme.statusBadgeText
                                )
                            }
                        }
                    }

                    // Completion Notes Section
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
                            Text(
                                "Completion Notes",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Text(
                                "Required",
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                color = GlassTheme.accent
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = notes,
                            onValueChange = { if (it.length <= 250) notes = it },
                            placeholder = {
                                Text(
                                    "Describe the completed task and any notes for the requester...",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = GlassTheme.textMuted
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 3,
                            enabled = !isSubmitting,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GlassTheme.cardBackground,
                                unfocusedContainerColor = GlassTheme.cardBackground,
                                focusedTextColor = GlassTheme.textPrimary,
                                unfocusedTextColor = GlassTheme.textPrimary,
                                focusedBorderColor = GlassTheme.accent,
                                unfocusedBorderColor = GlassTheme.cardBorder
                            )
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            "${notes.length}/250",
                            modifier = Modifier.align(Alignment.End),
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = GlassTheme.textMuted
                        )
                    }

                    // Proof Photo Upload Section
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
                            Text(
                                "Proof Photo",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Text(
                                text = if (selectedPhotoBytes != null) "Photo selected" else "Optional",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = if (selectedPhotoBytes != null) GlassTheme.statusBadgeText else GlassTheme.textMuted
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .clickable(enabled = !isSubmitting) {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                            shape = RoundedCornerShape(18.dp),
                            color = GlassTheme.chipBackground,
                            border = BorderStroke(1.dp, GlassTheme.chipBorder)
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
                                    shape = RoundedCornerShape(50),
                                    color = GlassTheme.pillButtonBg,
                                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = null,
                                            tint = GlassTheme.pillButtonText,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (selectedPhotoBytes != null) "Change Photo" else "Select Photo",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            ),
                                            color = GlassTheme.pillButtonText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                val canSubmit = !isSubmitting && !isRequester && !isAlreadySubmitted

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    BlackPillButton(
                        text = when {
                            isSubmitting -> "Submitting..."
                            isRequester -> "Requesters Cannot Submit Completion"
                            isAlreadySubmitted -> "Completion Already Submitted"
                            else -> "Submit Task Completion"
                        },
                        icon = if (!isSubmitting) Icons.Default.CheckCircle else null,
                        onClick = {
                            if (!canSubmit) return@BlackPillButton
                            if (notes.isBlank()) {
                                errorMessage = "Please enter completion notes describing the finished work."
                                return@BlackPillButton
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
                        enabled = canSubmit,
                        isFullWidth = true
                    )
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
