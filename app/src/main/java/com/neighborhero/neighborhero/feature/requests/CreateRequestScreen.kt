package com.neighborhero.neighborhero.feature.requests

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.nlp.NlpClassifier
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun CreateRequestScreen(
    onBackClick: () -> Unit,
    onSubmitRequest: (title: String, category: String, description: String, time: String, location: String) -> Unit
) {
    val context = LocalContext.current
    val nlpClassifier = remember { NlpClassifier(context) }

    var title by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Groceries") }
    var description by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("Maplewood District") }

    val categories = listOf("Groceries", "Repairs", "Pet Care", "Tutoring")

    // Real-time offline NLP classification
    val nlpSuggestedCategory = remember(title, description) {
        nlpClassifier.classifyRequestCategory("$title $description")
    }

    Scaffold(
        topBar = { AppTopBar(title = "Create Request", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                Text("Ask your neighbors", style = MaterialTheme.typography.headlineLarge, color = OnSurface)
                Text("Need a hand with something local? Drop your request below.", style = MaterialTheme.typography.bodyMedium, color = OnSurfaceVariant)
            }

            // NLP Smart Suggestion Pill
            AnimatedVisibility(visible = nlpSuggestedCategory != null && nlpSuggestedCategory != selectedCategory) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryFixed,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            nlpSuggestedCategory?.let { selectedCategory = it }
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .background(Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = OnPrimary, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Smart Suggestion: Auto-detected \"$nlpSuggestedCategory\". Tap to select.",
                            style = MaterialTheme.typography.labelMedium,
                            color = OnPrimaryFixed
                        )
                    }
                }
            }

            Text("Select Category", style = MaterialTheme.typography.labelLarge, color = OnSurface)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryContainer,
                            selectedLabelColor = OnPrimaryContainer
                        )
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Request Title") },
                placeholder = { Text("e.g., Need help picking up groceries") },
                leadingIcon = { Icon(Icons.Default.Title, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Detailed Description") },
                placeholder = { Text("Share specific details or tools needed...") },
                leadingIcon = { Icon(Icons.Default.EditNote, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Preferred Time & Date") },
                placeholder = { Text("e.g. Today, 4:00 PM") },
                leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            PrimaryButton(onClick = {
                if (title.isNotBlank()) {
                    onSubmitRequest(title, selectedCategory, description, time, location)
                }
            }) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Publish Request", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateRequestScreenPreview() {
    NeighborheroTheme {
        CreateRequestScreen(
            onBackClick = {},
            onSubmitRequest = { _, _, _, _, _ -> }
        )
    }
}
