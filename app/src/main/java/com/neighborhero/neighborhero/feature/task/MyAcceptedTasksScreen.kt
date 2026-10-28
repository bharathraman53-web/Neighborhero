package com.neighborhero.neighborhero.feature.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.RequestCard
import com.neighborhero.neighborhero.ui.theme.OnSurfaceVariant
import com.neighborhero.neighborhero.ui.theme.Primary

@Composable
fun MyAcceptedTasksScreen(
    tasks: List<HelpRequest>,
    onTaskClick: (HelpRequest) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "My Accepted Tasks", onBackClick = onBackClick) }
    ) { innerPadding ->
        if (tasks.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Handshake, contentDescription = null, tint = Primary, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No active accepted tasks", style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Browse nearby requests and offer help to become a local hero!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OnSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(tasks) { task ->
                    RequestCard(request = task, onClick = { onTaskClick(task) })
                }
            }
        }
    }
}
