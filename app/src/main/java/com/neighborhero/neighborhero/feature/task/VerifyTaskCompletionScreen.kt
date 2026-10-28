package com.neighborhero.neighborhero.feature.task

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun VerifyTaskCompletionScreen(
    task: HelpRequest? = null,
    onBackClick: () -> Unit,
    onVerified: (rating: Int, feedback: String) -> Unit
) {
    var rating by remember { mutableIntStateOf(5) }
    var reviewText by remember { mutableStateOf("") }
    val selectedTags = remember { mutableStateListOf<String>() }
    val tags = listOf("Super fast", "Very polite", "Excellent work", "Punctual")

    val helperName = task?.assignedHelperName ?: "Assigned Helper"
    val taskTitle = task?.title ?: "Community Assistance"

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

                    // Photo Evidence Grid
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
                                .background(SecondaryFixed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = helperName.split(" ").mapNotNull { it.firstOrNull() }.joinToString(""),
                                style = MaterialTheme.typography.labelLarge,
                                color = OnSecondaryFixed
                            )
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

            PrimaryButton(onClick = { onVerified(rating, reviewText) }) {
                Text("Confirm & Release Verification", style = MaterialTheme.typography.labelLarge)
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
