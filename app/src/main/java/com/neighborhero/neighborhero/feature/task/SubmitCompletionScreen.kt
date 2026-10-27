package com.neighborhero.neighborhero.feature.task

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.TaskAlt
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
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun SubmitCompletionScreen(
    onBackClick: () -> Unit,
    onSubmitSuccess: () -> Unit
) {
    var notes by remember { mutableStateOf("Cleared all weeds from Sector B, tilled garden beds, and spread organic mulch as requested.") }

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
                        Text("You're submitting completion for Community Garden Cleanup & Soil Prep.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
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
                            Text("Green Thumb Initiative", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
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
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceContainerLow,
                            unfocusedContainerColor = SurfaceContainerLow,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Text("${notes.length}/250", modifier = Modifier.align(Alignment.End), style = MaterialTheme.typography.bodySmall, color = Outline)
                }

                // Proof Photo Upload Box with Actual Garden Proof Image
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Proof Photo", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                painter = painterResource(id = R.drawable.img_proof_garden),
                                contentDescription = "Proof photo of garden",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Surface(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Text("Change Photo", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall, color = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(onClick = onSubmitSuccess) {
                Text("Submit Task Completion", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SubmitCompletionScreenPreview() {
    NeighborheroTheme {
        SubmitCompletionScreen(
            onBackClick = {},
            onSubmitSuccess = {}
        )
    }
}
