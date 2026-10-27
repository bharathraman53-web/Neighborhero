package com.neighborhero.neighborhero.feature.task

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun ConfirmTaskStartScreen(
    onBackClick: () -> Unit,
    onStartConfirmed: () -> Unit
) {
    var check1 by remember { mutableStateOf(false) }
    var check2 by remember { mutableStateOf(false) }
    var check3 by remember { mutableStateOf(false) }

    val allChecked = check1 && check2 && check3

    Scaffold(
        topBar = { AppTopBar(title = "Confirm Start", onBackClick = onBackClick) }
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
                // Status Greeting Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLow)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(PrimaryFixed, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Primary)
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text("Ready to begin?", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                            Text("Let's ensure everything is set for a safe visit.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                        }
                    }
                }

                // Task Summary Card with Actual Eleanor Vance Image Asset
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(shape = RoundedCornerShape(12.dp), color = SecondaryContainer) {
                                Text("Community Support", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = OnSecondaryContainer)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Agreed Time", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                Text("Today, 2:30 PM", style = MaterialTheme.typography.headlineSmall, color = Secondary, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text("Grocery Delivery & Assistance", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)

                        Spacer(modifier = Modifier.height(12.dp))

                        HorizontalDivider(color = SurfaceContainerHigh)

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceContainerHigh)
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.img_eleanor_vance),
                                        contentDescription = "Eleanor Vance",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column {
                                    Text("Eleanor Vance", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                                    Text("42 Maplewood Drive (0.8 mi away)", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                                }
                            }

                            IconButton(
                                onClick = {},
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(PrimaryFixed, CircleShape)
                            ) {
                                Icon(Icons.Default.Call, contentDescription = "Call", tint = Primary, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }

                // Pre-Task Safety Checklist
                Text("Pre-Task Safety Checklist", style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.SemiBold)

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = check1, onCheckedChange = { check1 = it }, colors = CheckboxDefaults.colors(checkedColor = Primary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Verify Community ID", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                                Text("Ensure your SafeNeighbour badge is visible upon arrival.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = SurfaceContainerLow)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = check2, onCheckedChange = { check2 = it }, colors = CheckboxDefaults.colors(checkedColor = Primary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Review Task Instructions", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                                Text("Double-check dietary requirements or drop-off notes.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = SurfaceContainerLow)

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(checked = check3, onCheckedChange = { check3 = it }, colors = CheckboxDefaults.colors(checkedColor = Primary))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text("Emergency Contact Ready", style = MaterialTheme.typography.labelLarge, color = OnSurface, fontWeight = FontWeight.Bold)
                                Text("Confirm support hotline is accessible in case of delays.", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                onClick = onStartConfirmed,
                enabled = allChecked
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm & Start Task", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ConfirmTaskStartScreenPreview() {
    NeighborheroTheme {
        ConfirmTaskStartScreen(
            onBackClick = {},
            onStartConfirmed = {}
        )
    }
}
