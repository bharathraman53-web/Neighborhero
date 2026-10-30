package com.neighborhero.neighborhero.feature.profile

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.NetworkImage
import com.neighborhero.neighborhero.ui.components.PrimaryButton
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

    Scaffold(
        topBar = { AppTopBar(title = "Edit Profile", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Error banner if any
            if (!validationError.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = validationError ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            // Profile Photo Picker Section
            Box(
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .size(108.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .border(3.dp, Primary, CircleShape)
                        .background(SurfaceContainerHigh)
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
                                style = MaterialTheme.typography.headlineLarge,
                                color = Primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = user.fullName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                            style = MaterialTheme.typography.headlineLarge,
                            color = Primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Camera Action Badge
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(Primary)
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
                        tint = OnPrimary,
                        modifier = Modifier.size(18.dp)
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
                    style = MaterialTheme.typography.labelLarge,
                    color = Secondary
                )
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Outline) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
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
                    leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = Outline) },
                    trailingIcon = {
                        Text(
                            text = "${phone.length}/10",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (phone.length == 10) Secondary else Outline,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    enabled = !isSaving
                )
            }

            OutlinedTextField(
                value = neighborhood,
                onValueChange = { neighborhood = it },
                label = { Text("Neighborhood") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Outline) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSaving
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 4,
                enabled = !isSaving
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                onClick = {
                    if (name.isBlank()) {
                        validationError = "Full Name cannot be empty."
                        return@PrimaryButton
                    }
                    if (phone.isNotEmpty() && phone.length != 10) {
                        validationError = "Phone number must be exactly 10 digits."
                        return@PrimaryButton
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
                enabled = !isSaving
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = OnPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Saving...", style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save Changes", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}
