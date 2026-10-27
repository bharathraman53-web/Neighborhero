package com.neighborhero.neighborhero.feature.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.Outline

@Composable
fun EditProfileScreen(
    user: User,
    onBackClick: () -> Unit,
    onSaveSuccess: (name: String, neighborhood: String, bio: String) -> Unit
) {
    var name by remember { mutableStateOf(user.fullName) }
    var neighborhood by remember { mutableStateOf(user.neighborhood) }
    var bio by remember { mutableStateOf(user.bio) }

    Scaffold(
        topBar = { AppTopBar(title = "Edit Profile", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Outline) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = neighborhood,
                onValueChange = { neighborhood = it },
                label = { Text("Neighborhood") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Outline) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Bio") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 4
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(onClick = { onSaveSuccess(name, neighborhood, bio) }) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Changes", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}
