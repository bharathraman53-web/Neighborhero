package com.neighborhero.neighborhero.feature.helpers

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.navigation.Routes
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.BottomNavigationBar
import com.neighborhero.neighborhero.ui.components.HelperCard

@Composable
fun RecommendedHelpersScreen(
    helpers: List<HelperProfile>,
    onHelperClick: (HelperProfile) -> Unit,
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title = "Recommended Helpers") },
        bottomBar = { BottomNavigationBar(currentRoute = Routes.RECOMMENDED_HELPERS, onNavigate = onNavigate) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(helpers) { helper ->
                HelperCard(helper = helper, onClick = { onHelperClick(helper) })
            }
        }
    }
}
