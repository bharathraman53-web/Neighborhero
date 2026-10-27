package com.neighborhero.neighborhero.feature.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.neighborhero.neighborhero.data.model.ChatMessage
import com.neighborhero.neighborhero.ui.components.AppTopBar
import com.neighborhero.neighborhero.ui.theme.*

@Composable
fun ChatScreen(
    messages: List<ChatMessage>,
    onSendMessage: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var inputText by remember { mutableStateOf("") }

    Scaffold(
        topBar = { AppTopBar(title = "Chat", onBackClick = onBackClick) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Task Context Banner
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = PrimaryContainer)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(Primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocalShipping, contentDescription = null, tint = OnPrimary)
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Active Task", style = MaterialTheme.typography.labelSmall, color = OnPrimaryContainer)
                            Text("Grocery Pickup & Delivery", style = MaterialTheme.typography.headlineSmall, color = OnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                    Surface(shape = RoundedCornerShape(12.dp), color = Primary) {
                        Text("In Progress", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, color = OnPrimary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Chat Messages Container
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(messages) { msg ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (msg.isUser) Arrangement.End else Arrangement.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (msg.isUser) Primary else SurfaceContainerLow
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    msg.messageText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (msg.isUser) OnPrimary else OnSurface
                                )

                                if (msg.attachmentCode != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Card(
                                        shape = RoundedCornerShape(8.dp),
                                        colors = CardDefaults.cardColors(containerColor = SurfaceContainerHigh)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(Icons.Default.VpnKey, contentDescription = null, tint = Secondary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(msg.attachmentTitle ?: "Code", style = MaterialTheme.typography.labelSmall, color = OnSurfaceVariant)
                                                Text(msg.attachmentCode, style = MaterialTheme.typography.headlineSmall, color = OnSurface, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    msg.timestamp,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (msg.isUser) OnPrimary.copy(alpha = 0.7f) else OnSurfaceVariant
                                )
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
                    AssistChip(
                        onClick = { onSendMessage(chipText) },
                        label = { Text(chipText) },
                        colors = AssistChipDefaults.assistChipColors(containerColor = SurfaceContainerLow)
                    )
                }
            }

            // Message Input Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { Text("Type a message to Eleanor...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = SurfaceContainerLow,
                        unfocusedContainerColor = SurfaceContainerLow,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(Primary, CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = OnPrimary)
                }
            }
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun ChatScreenPreview() {
    val sampleMessages = listOf(
        ChatMessage(
            id = "msg_1",
            senderId = "eleanor",
            senderName = "Eleanor Vance",
            senderInitials = "EV",
            messageText = "Hi there! Thank you so much for picking up my grocery list from Trader Joe's.",
            timestamp = "10:25 AM",
            isUser = false
        ),
        ChatMessage(
            id = "msg_2",
            senderId = "user_1",
            senderName = "You",
            senderInitials = "ME",
            messageText = "Yes, I got everything! On my way now.",
            timestamp = "10:28 AM",
            isUser = true
        )
    )
    NeighborheroTheme {
        ChatScreen(
            messages = sampleMessages,
            onSendMessage = {},
            onBackClick = {}
        )
    }
}
