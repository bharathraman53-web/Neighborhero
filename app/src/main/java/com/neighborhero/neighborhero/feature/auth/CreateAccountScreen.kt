package com.neighborhero.neighborhero.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun CreateAccountScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }

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
            // Header Icon & Title
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(PrimaryFixed, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = OnPrimaryFixed,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Join NeighborHero",
                style = MaterialTheme.typography.headlineLarge,
                color = OnSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Connect with verified helpers, lend a hand, and make your community safer together.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Registration Card Container (Surface Level 1)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerLowest),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Full Name
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Full Name", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        TextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = { Text("e.g. Sarah Jenkins") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = Outline) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedContainerColor = SurfaceContainerLow,
                                disabledContainerColor = SurfaceContainerLow,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    // Email Address
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Email Address", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        TextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = { Text("sarah@example.com") },
                            leadingIcon = { Icon(Icons.Default.Mail, contentDescription = null, tint = Outline) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedContainerColor = SurfaceContainerLow,
                                disabledContainerColor = SurfaceContainerLow,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    // Phone Number
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Phone Number", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        TextField(
                            value = phone,
                            onValueChange = { phone = it },
                            placeholder = { Text("(555) 000-0000") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null, tint = Outline) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedContainerColor = SurfaceContainerLow,
                                disabledContainerColor = SurfaceContainerLow,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    // Create Password
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Create Password", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        TextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = { Text("At least 8 characters") },
                            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Outline) },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = Outline
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedContainerColor = SurfaceContainerLow,
                                disabledContainerColor = SurfaceContainerLow,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    // Confirm Password
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Confirm Password", style = MaterialTheme.typography.labelMedium, color = OnSurfaceVariant)
                        TextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = { Text("Re-enter your password") },
                            leadingIcon = { Icon(Icons.Default.LockReset, contentDescription = null, tint = Outline) },
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = SurfaceContainerLow,
                                unfocusedContainerColor = SurfaceContainerLow,
                                disabledContainerColor = SurfaceContainerLow,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = Color.Transparent
                            )
                        )
                    }

                    // Terms Checkbox
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Checkbox(
                            checked = agreeTerms,
                            onCheckedChange = { agreeTerms = it },
                            colors = CheckboxDefaults.colors(checkedColor = Secondary)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "I agree to NeighborHero's Guidelines & Terms.",
                            style = MaterialTheme.typography.bodySmall,
                            color = OnSurfaceVariant
                        )
                    }

                    // Sign Up Button
                    PrimaryButton(onClick = onSignUpSuccess) {
                        Text("Sign Up as NeighborHero", style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Footer Link
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant
                )
                TextButton(onClick = onLoginClick) {
                    Text("Log In", style = MaterialTheme.typography.labelLarge, color = Secondary)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CreateAccountScreenPreview() {
    NeighborheroTheme {
        CreateAccountScreen(
            onSignUpSuccess = {},
            onLoginClick = {}
        )
    }
}
