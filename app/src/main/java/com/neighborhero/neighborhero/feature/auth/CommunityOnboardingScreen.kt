package com.neighborhero.neighborhero.feature.auth

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.R
import com.neighborhero.neighborhero.ui.components.BlackPillButton
import com.neighborhero.neighborhero.ui.components.GlassTheme
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground

@Composable
fun CommunityOnboardingScreen(
    onContinueClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) }

    WaterScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Skip Action (over ocean gradient)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkipClick) {
                    Text(
                        text = "Skip",
                        color = Color.White.copy(alpha = 0.9f),
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            // Step Indicator (capsule dots)
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    val isCurrent = (index + 1) == step
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .width(if (isCurrent) 28.dp else 8.dp)
                            .background(
                                color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.40f),
                                shape = RoundedCornerShape(3.dp)
                            )
                    )
                }
            }

            // Main Illustration Container with Frosted Floating Badges
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1.05f)
                    .shadow(8.dp, RoundedCornerShape(26.dp), spotColor = Color(0x222C4663)),
                shape = RoundedCornerShape(26.dp),
                color = Color.White.copy(alpha = 0.70f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.90f))
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_onboarding_hero),
                        contentDescription = "Community onboarding illustration",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Top-Left Floating Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(14.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = GlassTheme.cardBackground,
                        border = BorderStroke(1.dp, GlassTheme.cardBorder),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(GlassTheme.accentSoft, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = GlassTheme.accent,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text("Grocery run", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), fontWeight = FontWeight.Bold, color = GlassTheme.textPrimary)
                                Text("Completed 2m ago", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = GlassTheme.textMuted)
                            }
                        }
                    }

                    // Bottom-Right Floating Badge
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(14.dp),
                        shape = RoundedCornerShape(14.dp),
                        color = GlassTheme.cardBackground,
                        border = BorderStroke(1.dp, GlassTheme.cardBorder),
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(30.dp)
                                    .background(Color(0xFF0F766E).copy(alpha = 0.20f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Handshake,
                                    contentDescription = null,
                                    tint = if (GlassTheme.isDark) Color(0xFF2DD4BF) else Color(0xFF0F766E),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text("Trusted Helper", style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp), fontWeight = FontWeight.Bold, color = GlassTheme.textPrimary)
                                Text("Verified Neighbor", style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp), color = GlassTheme.textMuted)
                            }
                        }
                    }
                }
            }

            // Headline & Description (Frosted Glass Container)
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = GlassTheme.cardBackground,
                border = BorderStroke(1.dp, GlassTheme.cardBorder),
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = when (step) {
                            1 -> "Neighbors helping neighbors, effortlessly."
                            2 -> "Request local assistance anytime."
                            else -> "Build a safer, friendlier neighborhood."
                        },
                        style = MaterialTheme.typography.headlineLarge.copy(
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.4).sp
                        ),
                        color = GlassTheme.textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = when (step) {
                            1 -> "Connect with trusted people right down your street for quick errands, borrowed tools, and friendly local support."
                            2 -> "Post a request for groceries, repairs, pet sitting, or tutoring and let verified local heroes lend a hand."
                            else -> "Join over 1,200 active neighbors making local communities warmer, safer, and better connected."
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        ),
                        color = GlassTheme.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Action Buttons & Verification Footer
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BlackPillButton(
                    text = if (step < 3) "Continue" else "Get Started",
                    onClick = {
                        if (step < 3) step += 1 else onContinueClick()
                    },
                    isFullWidth = true,
                    icon = Icons.AutoMirrored.Filled.ArrowForward
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = GlassTheme.accent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Secure & community-verified",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = GlassTheme.textMuted
                    )
                }
            }
        }
    }
}
