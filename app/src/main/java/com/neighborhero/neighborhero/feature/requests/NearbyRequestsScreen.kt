package com.neighborhero.neighborhero.feature.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.components.RequestCard
import com.neighborhero.neighborhero.ui.theme.SecondaryContainer
import com.neighborhero.neighborhero.ui.theme.SurfaceContainerLow

@Composable
fun NearbyRequestsScreen(
    requests: List<HelpRequest>,
    onRequestClick: (HelpRequest) -> Unit,
    onNavigate: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Groceries", "Repairs", "Pet Care", "Tutoring")

    val filtered = if (selectedCategory == "All") {
        requests
    } else {
        requests.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            AppTopBar(title = "Nearby Requests")
        },
        bottomBar = {
            BottomNavigationBar(currentRoute = Routes.NEARBY_REQUESTS, onNavigate = onNavigate)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                items(categories) { cat ->
                    FilterChip(
                        selected = selectedCategory == cat,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SecondaryContainer
                        )
                    )
                }
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filtered) { req ->
                    RequestCard(request = req, onClick = { onRequestClick(req) })
                }
            }
        }
    }
}
