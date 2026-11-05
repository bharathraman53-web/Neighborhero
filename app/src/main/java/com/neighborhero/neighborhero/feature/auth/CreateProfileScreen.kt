package com.neighborhero.neighborhero.feature.auth

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.data.model.RequestCategory
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassTextField
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground

@Composable
fun CreateProfileScreen(
    initialName: String = "",
    onProfileCompleted: (name: String, neighborhood: String, bio: String) -> Unit = { _, _, _ -> },
    onProfileCompletedWithPhoto: ((name: String, neighborhood: String, bio: String, photoBytes: ByteArray?, mime: String, filename: String) -> Unit)? = null
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(initialName) }
    var neighborhood by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val selectedSkills = remember { mutableStateListOf<String>() }

    var selectedPhotoBytes by remember { mutableStateOf<ByteArray?>(null) }
    var selectedPhotoMime by remember { mutableStateOf("image/jpeg") }
    var selectedPhotoName by remember { mutableStateOf("avatar.jpg") }

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
                    selectedPhotoBytes = bytes
                }
            } catch (_: Exception) {}
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

    val allSkills = RequestCategory.CANONICAL + listOf("Tutoring", "Moving", "Yardwork")

    WaterScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header over Ocean Gradient
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Your Neighbor Persona",
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp
                        ),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Shape how your community sees you",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = Color.White.copy(alpha = 0.88f)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.22f))
                        .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.40f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Photo Upload Frosted Card
            GlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                shape = RoundedCornerShape(26.dp),
                alpha = 0.82f,
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(3.dp, Color.White, CircleShape)
                                .background(Color(0xFFE2E8F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (previewBitmap != null) {
                                Image(
                                    bitmap = previewBitmap,
                                    contentDescription = "Selected profile photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = R.drawable.img_create_profile_user),
                                    contentDescription = "Profile photo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }

                        // Black Circle Add Badge
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomEnd)
                                .background(Color(0xFF0F172A), CircleShape)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add photo", tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }

                    Text(
                        text = "Upload Your Hero Photo",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GlassTheme.textPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Let your neighbors put a friendly face to your good deeds.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = GlassTheme.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Form Inputs in Frosted Glass Container
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                alpha = 0.82f,
                elevation = 6.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Full Name
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Full Name",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        GlassTextField(
                            value = name,
                            onValueChange = { name = it },
                            placeholder = "e.g. Eleanor Vance",
                            leadingIcon = Icons.Default.Badge,
                            singleLine = true
                        )
                    }

                    // Neighborhood / Area
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Neighborhood / Area",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        GlassTextField(
                            value = neighborhood,
                            onValueChange = { neighborhood = it },
                            placeholder = "e.g. Maplewood & Oakridge",
                            leadingIcon = Icons.Default.LocationOn,
                            singleLine = true
                        )
                    }

                    // Bio Description
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            "Bio Description",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        GlassTextField(
                            value = bio,
                            onValueChange = { bio = it },
                            placeholder = "Tell neighbors how you'd love to help...",
                            singleLine = false,
                            maxLines = 4
                        )
                    }

                    // Skills Filter Chips
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "How can you help? (Skills)",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            allSkills.take(3).forEach { skill ->
                                val selected = selectedSkills.contains(skill)
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (selected) GlassTheme.chipSelectedBg else GlassTheme.chipBackground,
                                    border = BorderStroke(1.dp, if (selected) GlassTheme.chipSelectedBg else GlassTheme.chipBorder),
                                    modifier = Modifier.clickable {
                                        if (selected) selectedSkills.remove(skill) else selectedSkills.add(skill)
                                    }
                                ) {
                                    Text(
                                        text = skill,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        ),
                                        color = if (selected) GlassTheme.chipSelectedText else GlassTheme.chipText
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Complete Profile Button (Pitch Black Pill)
                    BlackPillButton(
                        text = "Complete Profile",
                        onClick = {
                            if (onProfileCompletedWithPhoto != null) {
                                onProfileCompletedWithPhoto(name, neighborhood, bio, selectedPhotoBytes, selectedPhotoMime, selectedPhotoName)
                            } else {
                                onProfileCompleted(name, neighborhood, bio)
                            }
                        },
                        isFullWidth = true,
                        icon = Icons.Default.Check
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
