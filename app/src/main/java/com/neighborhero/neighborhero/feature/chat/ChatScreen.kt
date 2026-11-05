package com.neighborhero.neighborhero.feature.chat

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.neighborhero.neighborhero.data.model.ChatMessage
import com.neighborhero.neighborhero.ui.components.GlassCard
import com.neighborhero.neighborhero.ui.components.GlassEmptyState
import com.neighborhero.neighborhero.ui.components.GlassFocusCard
import com.neighborhero.neighborhero.ui.components.GlassTopBar
import com.neighborhero.neighborhero.ui.components.WaterScreenBackground
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit,
    recipientName: String? = null,
    isSending: Boolean = false
) {
    var inputText by remember { mutableStateOf("") }
    val displayTitle = if (!recipientName.isNullOrBlank()) recipientName else "Neighbor Chat"

    WaterScreenBackground {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { GlassTopBar(title = displayTitle, onBackClick = onBackClick) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                // Task Context Banner
                GlassFocusCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(GlassTheme.accent.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ChatBubble,
                                    contentDescription = null,
                                    tint = GlassTheme.accent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Direct Neighbor Messaging",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                                    color = GlassTheme.textMuted
                                )
                                Text(
                                    displayTitle,
                                    style = MaterialTheme.typography.headlineSmall.copy(
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = GlassTheme.textPrimary
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GlassTheme.statusBadgeBg,
                            border = BorderStroke(1.dp, GlassTheme.statusBadgeText.copy(alpha = 0.35f))
                        ) {
                            Text(
                                "Active",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = GlassTheme.statusBadgeText
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Chat Messages Container
                if (messages.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassEmptyState(
                            icon = Icons.Default.ChatBubble,
                            title = "No messages yet",
                            subtitle = "Send a message to coordinate privately with your neighbor."
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(messages, key = { it.id }) { msg ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                            ) {
                                Surface(
                                    shape = if (msg.isUser) {
                                        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
                                    } else {
                                        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
                                    },
                                    color = if (msg.isUser) GlassTheme.pillButtonBg else GlassTheme.cardBackground,
                                    border = BorderStroke(
                                        1.dp,
                                        if (msg.isUser) GlassTheme.pillButtonBg else GlassTheme.cardBorder
                                    ),
                                    shadowElevation = 3.dp,
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Text(
                                            msg.messageText,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                lineHeight = 20.sp
                                            ),
                                            color = if (msg.isUser) GlassTheme.pillButtonText else GlassTheme.textPrimary
                                        )

                                        if (msg.attachmentCode != null) {
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(12.dp),
                                                color = if (msg.isUser) GlassTheme.pillButtonText.copy(alpha = 0.15f) else GlassTheme.chipBackground,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (msg.isUser) GlassTheme.pillButtonText.copy(alpha = 0.25f) else GlassTheme.chipBorder
                                                )
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(10.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        Icons.Default.VpnKey,
                                                        contentDescription = null,
                                                        tint = if (msg.isUser) GlassTheme.pillButtonText else GlassTheme.accent,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Column {
                                                        Text(
                                                            msg.attachmentTitle ?: "Code",
                                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                                            color = if (msg.isUser) GlassTheme.pillButtonText.copy(alpha = 0.7f) else GlassTheme.textMuted
                                                        )
                                                        Text(
                                                            msg.attachmentCode,
                                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                                fontSize = 15.sp,
                                                                fontWeight = FontWeight.Bold
                                                            ),
                                                            color = if (msg.isUser) GlassTheme.pillButtonText else GlassTheme.textPrimary
                                                        )
                                                    }
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            msg.timestamp,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                            color = if (msg.isUser) GlassTheme.pillButtonText.copy(alpha = 0.7f) else GlassTheme.textMuted,
                                            modifier = Modifier.align(Alignment.End)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Reply Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 8.dp)
                ) {
                    items(listOf("I'm on my way!", "Got it, thanks!", "Running 5 mins late")) { chipText ->
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = GlassTheme.chipBackground,
                            border = BorderStroke(1.dp, GlassTheme.chipBorder),
                            modifier = Modifier.clickable(enabled = !isSending) {
                                onSendMessage(chipText)
                            }
                        ) {
                            Text(
                                chipText,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                ),
                                color = GlassTheme.chipText
                            )
                        }
                    }
                }

                // Message Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = {
                            Text(
                                "Type a private message...",
                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                color = GlassTheme.textMuted
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(50),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = GlassTheme.cardBackground,
                            unfocusedContainerColor = GlassTheme.cardBackground,
                            focusedTextColor = GlassTheme.textPrimary,
                            unfocusedTextColor = GlassTheme.textPrimary,
                            focusedBorderColor = GlassTheme.accent,
                            unfocusedBorderColor = GlassTheme.cardBorder
                        ),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    val canSend = !isSending && inputText.isNotBlank()
                    Surface(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .shadow(if (canSend) 4.dp else 0.dp, CircleShape)
                            .clickable(enabled = canSend) {
                                val textToSend = inputText
                                inputText = ""
                                onSendMessage(textToSend)
                            },
                        shape = CircleShape,
                        color = if (canSend) GlassTheme.pillButtonBg else GlassTheme.chipBackground
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = if (canSend) GlassTheme.pillButtonText else GlassTheme.textMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    NeighborheroTheme {
        ChatScreen(
            messages = emptyList(),
            onSendMessage = {},
            onBackClick = {},
            recipientName = "Sarah Jenkins"
        )
    }
}
