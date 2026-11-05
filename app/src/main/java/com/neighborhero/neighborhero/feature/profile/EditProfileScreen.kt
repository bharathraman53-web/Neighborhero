package com.neighborhero.neighborhero.feature.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun EditProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onSaveSuccess: (name: String, neighborhood: String, bio: String) -> Unit,
    onSaveSuccessWithPhone: ((name: String, neighborhood: String, bio: String, phone: String) -> Unit)? = null,
    onSaveWithPhoto: ((name: String, neighborhood: String, bio: String, phone: String, photoBytes: ByteArray?, mimeType: String, fileName: String, onDone: (Boolean, String?) -> Unit) -> Unit)? = null
) {
    val context = LocalContext.current

    var name by remember { mutableStateOf(user.fullName) }
    var neighborhood by remember { mutableStateOf(user.neighborhood) }
    var bio by remember { mutableStateOf(user.bio) }
    var phone by remember { mutableStateOf(user.phone.filter { it.isDigit() }.take(10)) }

    var selectedPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedPhotoMime by remember { mutableStateOf("image/jpeg") }
    var selectedPhotoName by remember { mutableStateOf("avatar.jpg") }

    var validationError by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val mime = context.contentResolver.getType(uri) ?: "image/jpeg"
                selectedPhotoMime = mime
                selectedPhotoName = when {
                    mime.contains("png", ignoreCase = true) -> "avatar.png"
                    mime.contains("webp", ignoreCase = true) -> "avatar.webp"
                    else -> "avatar.jpg"
                }
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                if (bytes != null && bytes.isNotEmpty()) {
                    if (bytes.size > 5 * 1024 * 1024) {
                        validationError = "Selected photo exceeds 5 MB limit. Please choose a smaller image."
                    } else {
                        selectedPhotoBytes = bytes
                        validationError = null
                    }
                }
            } catch (e: Exception) {
                validationError = "Failed to load selected photo: ${e.message}"
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
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GlassTopBar(title = "Edit Profile", onBackClick = onBackClick) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Error banner
                if (!validationError.isNullOrBlank()) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GlassTheme.warningBadgeBg,
                        border = BorderStroke(1.dp, GlassTheme.warningBadgeText.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = validationError ?: "",
                            color = GlassTheme.warningBadgeText,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }

                // Profile Photo Card
                GlassFocusCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(108.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(100.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, GlassTheme.accent, CircleShape)
                                    .background(GlassTheme.accent.copy(alpha = 0.15f))
                                    .clickable(enabled = !isSaving) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (previewBitmap != null) {
                                    Image(
                                        bitmap = previewBitmap,
                                        contentDescription = "Selected profile photo",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else if (user.avatarUrl.isNotBlank()) {
                                    NetworkImage(
                                        url = user.avatarUrl,
                                        modifier = Modifier.fillMaxSize(),
                                        contentDescription = "Profile photo"
                                    ) {
                                        Text(
                                            text = user.fullName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                            color = GlassTheme.accent
                                        )
                                    }
                                } else {
                                    Text(
                                        text = user.fullName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                                        color = GlassTheme.accent
                                    )
                                }
                            }

                            // Camera Action Badge
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(GlassTheme.pillButtonBg)
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable(enabled = !isSaving) {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Change photo",
                                    tint = GlassTheme.pillButtonText,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isSaving
                        ) {
                            Text(
                                text = if (selectedPhotoBytes != null) "Change Selected Photo" else "Upload Profile Photo",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.accent
                            )
                        }
                    }
                }

                // Profile Fields Card
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    alpha = 0.88f
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        Text(
                            "Account Details",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )

                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = GlassTheme.accent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isSaving,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GlassTheme.cardBackground,
                                unfocusedContainerColor = GlassTheme.cardBackground,
                                focusedTextColor = GlassTheme.textPrimary,
                                unfocusedTextColor = GlassTheme.textPrimary,
                                focusedBorderColor = GlassTheme.accent,
                                unfocusedBorderColor = GlassTheme.cardBorder
                            )
                        )

                        // Phone (10 digits)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = phone,
                                onValueChange = { input ->
                                    phone = input.filter { it.isDigit() }.take(10)
                                },
                                label = { Text("Phone Number (10 digits)") },
                                placeholder = { Text("10-digit mobile number") },
                                leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = GlassTheme.accent) },
                                trailingIcon = {
                                    Text(
                                        text = "${phone.length}/10",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                        color = if (phone.length == 10) GlassTheme.statusBadgeText else GlassTheme.textMuted,
                                        modifier = Modifier.padding(end = 12.dp)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                enabled = !isSaving,
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

                        OutlinedTextField(
                            value = neighborhood,
                            onValueChange = { neighborhood = it },
                            label = { Text("Neighborhood") },
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = GlassTheme.accent) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            enabled = !isSaving,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = GlassTheme.cardBackground,
                                unfocusedContainerColor = GlassTheme.cardBackground,
                                focusedTextColor = GlassTheme.textPrimary,
                                unfocusedTextColor = GlassTheme.textPrimary,
                                focusedBorderColor = GlassTheme.accent,
                                unfocusedBorderColor = GlassTheme.cardBorder
                            )
                        )

                        OutlinedTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            label = { Text("Bio") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            minLines = 4,
                            enabled = !isSaving,
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

                Spacer(modifier = Modifier.height(10.dp))

                Box(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    BlackPillButton(
                        text = if (isSaving) "Saving..." else "Save Changes",
                        icon = if (!isSaving) Icons.Default.Check else null,
                        onClick = {
                            if (name.isBlank()) {
                                validationError = "Full Name cannot be empty."
                                return@BlackPillButton
                            }
                            if (phone.isNotEmpty() && phone.length != 10) {
                                validationError = "Phone number must be exactly 10 digits."
                                return@BlackPillButton
                            }
                            validationError = null
                            isSaving = true

                            val photo = selectedPhotoBytes
                            if (onSaveWithPhoto != null) {
                                onSaveWithPhoto(
                                    name.trim(),
                                    neighborhood.trim(),
                                    bio.trim(),
                                    phone,
                                    photo,
                                    selectedPhotoMime,
                                    selectedPhotoName
                                ) { success, errorMsg ->
                                    isSaving = false
                                    if (!success) {
                                        validationError = errorMsg ?: "Failed to save profile changes."
                                    }
                                }
                            } else if (onSaveSuccessWithPhone != null) {
                                onSaveSuccessWithPhone(name.trim(), neighborhood.trim(), bio.trim(), phone)
                                isSaving = false
                            } else {
                                onSaveSuccess(name.trim(), neighborhood.trim(), bio.trim())
                                isSaving = false
                            }
                        },
                        enabled = !isSaving,
                        isFullWidth = true
                    )
                }
            }
        }
    }
}
