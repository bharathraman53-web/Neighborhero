package com.neighborhero.neighborhero.feature.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassTextField
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground

@Composable
fun CreateAccountScreen(
    onSignUpSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    onSignUpSubmit: ((fullName: String, email: String, phone: String, pass: String) -> Unit)? = null,
    errorMessage: String? = null
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var agreeTerms by remember { mutableStateOf(false) }
    var localValidationErr by remember { mutableStateOf<String?>(null) }

    WaterScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Emblem & Title (Oceanic Teal Gradient Top)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.22f))
                    .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.40f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Join NeighborHero",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Connect with verified helpers, lend a hand, and make your community safer together.",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                color = Color.White.copy(alpha = 0.88f),
                modifier = Modifier.widthIn(max = 300.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Registration Frosted Glass Card Container
            GlassCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                alpha = 0.82f,
                elevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    val displayError = localValidationErr ?: errorMessage
                    if (!displayError.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFFFE4E6),
                            border = BorderStroke(1.dp, Color(0xFFFDA4AF)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = displayError,
                                color = Color(0xFFE11D48),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    // Full Name
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Full Name",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        GlassTextField(
                            value = fullName,
                            onValueChange = { fullName = it },
                            placeholder = "e.g. Sarah Jenkins",
                            leadingIcon = Icons.Default.Person,
                            singleLine = true
                        )
                    }

                    // Email Address
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Email Address",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        GlassTextField(
                            value = email,
                            onValueChange = { email = it },
                            placeholder = "sarah@example.com",
                            leadingIcon = Icons.Default.Mail,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next)
                        )
                    }

                    // Phone Number
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Mobile Number (10 digits)",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.textPrimary
                            )
                            Text(
                                "${phone.length}/10",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (phone.length == 10) GlassTheme.accent else GlassTheme.textMuted
                            )
                        }
                        GlassTextField(
                            value = phone,
                            onValueChange = { input -> phone = input.filter { it.isDigit() }.take(10) },
                            placeholder = "10-digit mobile number",
                            leadingIcon = Icons.Default.Call,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next)
                        )
                    }

                    // Create Password
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Create Password",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        GlassTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "At least 6 characters",
                            leadingIcon = Icons.Default.Lock,
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = GlassTheme.textMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )
                    }

                    // Confirm Password
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            "Confirm Password",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = GlassTheme.textPrimary
                        )
                        GlassTextField(
                            value = confirmPassword,
                            onValueChange = { confirmPassword = it },
                            placeholder = "Re-enter your password",
                            leadingIcon = Icons.Default.LockReset,
                            singleLine = true,
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation()
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
                            colors = CheckboxDefaults.colors(
                                checkedColor = GlassTheme.pillButtonBg,
                                checkmarkColor = GlassTheme.pillButtonText,
                                uncheckedColor = GlassTheme.textMuted
                            )
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "I agree to NeighborHero's Guidelines & Terms.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = GlassTheme.textSecondary
                        )
                    }

                    // Sign Up Pitch-Black Pill Button
                    BlackPillButton(
                        text = "Sign Up as NeighborHero",
                        onClick = {
                            if (fullName.isBlank() || email.isBlank() || password.isBlank()) {
                                localValidationErr = "Please complete all required fields."
                                return@BlackPillButton
                            }
                            if (phone.length != 10) {
                                localValidationErr = "Phone number must be exactly 10 digits."
                                return@BlackPillButton
                            }
                            if (password != confirmPassword) {
                                localValidationErr = "Passwords do not match."
                                return@BlackPillButton
                            }
                            if (password.length < 6) {
                                localValidationErr = "Password must be at least 6 characters."
                                return@BlackPillButton
                            }
                            if (!agreeTerms) {
                                localValidationErr = "Please agree to NeighborHero's Guidelines & Terms to continue."
                                return@BlackPillButton
                            }
                            localValidationErr = null
                            if (onSignUpSubmit != null) {
                                onSignUpSubmit(fullName.trim(), email.trim(), phone.trim(), password)
                            } else {
                                onSignUpSuccess()
                            }
                        },
                        isFullWidth = true,
                        icon = Icons.AutoMirrored.Filled.ArrowForward
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Footer Link
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Already have an account? ",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                    color = GlassTheme.textSecondary
                )
                TextButton(onClick = onLoginClick) {
                    Text(
                        text = "Log In",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = GlassTheme.accent
                    )
                }
            }
        }
    }
}
