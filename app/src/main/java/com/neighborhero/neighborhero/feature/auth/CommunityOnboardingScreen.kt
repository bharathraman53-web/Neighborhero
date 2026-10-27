package com.neighborhero.neighborhero.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun CommunityOnboardingScreen(
    onContinueClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Skip Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkipClick) {
                    Text("Skip", color = OnSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                }
            }

            // Step Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isCurrent = (index + 1) == step
                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(if (isCurrent) 32.dp else 8.dp)
                            .background(
                                color = if (isCurrent) Primary else SurfaceVariant,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            // Main Illustration Container with Actual Image & Floating Badges
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryFixed.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Actual Onboarding Street Illustration
                    Image(
                        painter = painterResource(id = R.drawable.img_onboarding_hero),
                        contentDescription = "Community onboarding illustration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Top-Left Floating Badge ("Grocery run")
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Surface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Secondary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = OnSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text("Grocery run", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text("Completed 2m ago", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                    }

                    // Bottom-Right Floating Badge ("Trusted Helper")
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(16.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Surface.copy(alpha = 0.92f),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(Primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = null,
                                    tint = OnPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text("Trusted Helper", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = OnSurface)
                                Text("Verified Neighbor", style = MaterialTheme.typography.bodySmall, color = OnSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // Headline & Description
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = when (step) {
                        1 -> "Neighbors helping neighbors, effortlessly."
                        2 -> "Request local assistance anytime."
                        else -> "Build a safer, friendlier neighborhood."
                    },
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = when (step) {
                        1 -> "Connect with trusted people right down your street for quick errands, borrowed tools, and friendly local support."
                        2 -> "Post a request for groceries, repairs, pet sitting, or tutoring and let verified local heroes lend a hand."
                        else -> "Join over 1,200 active neighbors making local communities warmer, safer, and better connected."
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    color = OnSurfaceVariant
                )
            }

            // Action Buttons & Verification Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(
                    onClick = {
                        if (step < 3) step += 1 else onContinueClick()
                    }
                ) {
                    Text(if (step < 3) "Continue" else "Get Started", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = Secondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Secure & community-verified",
                        style = MaterialTheme.typography.bodySmall,
                        color = OnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun CommunityOnboardingScreenPreview() {
    NeighborheroTheme {
        CommunityOnboardingScreen(
            onContinueClick = {},
            onSkipClick = {}
        )
    }
}
