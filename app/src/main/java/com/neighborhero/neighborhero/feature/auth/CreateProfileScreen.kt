package com.neighborhero.neighborhero.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun CreateProfileScreen(
    initialName: String = "",
    onProfileCompleted: (name: String, neighborhood: String, bio: String) -> Unit = { _, _, _ -> }
) {
    var name by remember { mutableStateOf(initialName) }
    var neighborhood by remember { mutableStateOf("") }
    var bio by remember { mutableStateOf("") }
    val selectedSkills = remember { mutableStateListOf<String>() }

    val allSkills = listOf("Groceries", "Repairs", "Pet Care", "Tutoring", "Moving", "Yardwork")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Your Neighbor Persona", style = MaterialTheme.typography.headlineLarge, color = OnSurface)
                    Text("Shape how your community sees you", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
                }

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(SecondaryContainer, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Eco, contentDescription = null, tint = OnSecondaryContainer)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Profile Photo Upload Card with Actual Stitch Asset
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(modifier = Modifier.padding(bottom = 12.dp)) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .border(4.dp, Surface, CircleShape)
                                .background(SurfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_create_profile_user),
                                contentDescription = "Profile photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .align(Alignment.BottomEnd)
                                .background(Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add photo", tint = OnPrimary, modifier = Modifier.size(16.dp))
                        }
                    }

                    Text("Upload Your Hero Photo", style = MaterialTheme.typography.headlineSmall, color = OnSurface)
                    Text("Let your neighbors put a friendly face to your good deeds.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Form Inputs
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Full Name", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        placeholder = { Text("e.g. Eleanor Vance") },
                        leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Outline) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Neighborhood / Area", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    OutlinedTextField(
                        value = neighborhood,
                        onValueChange = { neighborhood = it },
                        placeholder = { Text("e.g. Maplewood & Oakridge") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Outline) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Bio Description", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    OutlinedTextField(
                        value = bio,
                        onValueChange = { bio = it },
                        placeholder = { Text("Tell neighbors how you'd love to help...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        minLines = 3
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("How can you help? (Skills)", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allSkills.take(3).forEach { skill ->
                            val selected = selectedSkills.contains(skill)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (selected) selectedSkills.remove(skill) else selectedSkills.add(skill)
                                },
                                label = { Text(skill) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = SecondaryContainer,
                                    selectedLabelColor = OnSecondaryContainer
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                PrimaryButton(onClick = {
                    onProfileCompleted(name, neighborhood, bio)
                }) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Complete Profile", style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateProfileScreenPreview() {
    NeighborheroTheme {
        CreateProfileScreen(
            onProfileCompleted = { _, _, _ -> }
        )
    }
}
