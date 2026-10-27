package com.neighborhero.neighborhero.feature.welcome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.ui.components.PrimaryButton
import com.neighborhero.neighborhero.ui.theme.NeighborheroTheme
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun WelcomeScreen(
    onGetStartedClick: () -> Unit,
    onLoginClick: () -> Unit
) {
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
            // Top Branding Header
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(PrimaryFixed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Handshake,
                        contentDescription = "Handshake",
                        tint = Primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "NeighborHero",
                    style = MaterialTheme.typography.headlineLarge,
                    color = OnSurface
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Connecting friendly neighbors for everyday help, local support, and stronger community bonds.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = OnSurfaceVariant,
                    modifier = Modifier.widthIn(max = 280.dp)
                )
            }

            // Hero Card Illustration with Actual Image Asset & Overlay Badges
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(16.dp), // Matched Stitch rounded-xl
                colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Actual Stitch Local Hero Image Asset
                    Image(
                        painter = painterResource(id = R.drawable.img_welcome_hero),
                        contentDescription = "Friendly diverse neighbors illustration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Gradient Overlay Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Primary.copy(alpha = 0.4f),
                                        Primary.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )

                    // Floating Pill Badge Overlay at Bottom
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .align(Alignment.BottomCenter),
                        shape = RoundedCornerShape(12.dp),
                        color = Surface.copy(alpha = 0.92f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(SecondaryContainer, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = OnSecondaryContainer,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Trusted Local Network",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = OnSurface,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Over 1,200 neighbors active near you",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = OnSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons & Guidelines Link Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PrimaryButton(onClick = onGetStartedClick) {
                    Text("Get Started", style = MaterialTheme.typography.labelLarge)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Filled Secondary Container Login Button (Matches Stitch bg-surface-container-low)
                Button(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SurfaceContainerLow,
                        contentColor = Primary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                ) {
                    Text("Login", style = MaterialTheme.typography.labelLarge, color = Primary, fontWeight = FontWeight.SemiBold)
                }

                Text(
                    text = "By continuing, you agree to NeighborHero's Community Guidelines.",
                    style = MaterialTheme.typography.bodySmall,
                    color = OnSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun WelcomeScreenPreview() {
    NeighborheroTheme {
        WelcomeScreen(
            onGetStartedClick = {},
            onLoginClick = {}
        )
    }
}
