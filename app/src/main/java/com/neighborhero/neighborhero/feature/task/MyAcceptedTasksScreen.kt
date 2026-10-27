package com.neighborhero.neighborhero.feature.task

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.RequestCard

@Composable
fun MyAcceptedTasksScreen(
    tasks: List<HelpRequest>,
    onTaskClick: (HelpRequest) -> Unit,
    onBackClick: () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "My Accepted Tasks", onBackClick = onBackClick) }
    ) { innerPadding ->
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
