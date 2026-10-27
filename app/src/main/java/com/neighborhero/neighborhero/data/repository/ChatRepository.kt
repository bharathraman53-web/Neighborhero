package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.model.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository {
    private val initialMessages = listOf(
        ChatMessage(
            id = "msg_1",
            senderId = "eleanor_vance",
            senderName = "Eleanor Vance",
            senderInitials = "EV",
            messageText = "Hi there! Thank you so much for picking up my grocery list from Trader Joe's. Did you manage to find the organic whole milk?",
            timestamp = "10:25 AM",
            isUser = false
        ),
        ChatMessage(
            id = "msg_2",
            senderId = "user_1",
            senderName = "You",
            senderInitials = "ME",
            messageText = "Yes, I got the last two bottles! Also picked up the fresh bakery sourdough you requested.",
            timestamp = "10:28 AM",
            isUser = true
        ),
        ChatMessage(
            id = "msg_3",
            senderId = "eleanor_vance",
            senderName = "Eleanor Vance",
            senderInitials = "EV",
            messageText = "Oh you're a lifesaver! Here is the entry gate code just in case the front buzzer is acting up:",
            timestamp = "10:30 AM",
            isUser = false,
            attachmentCode = "#4921",
            attachmentTitle = "Gate Code"
        )
    )

    private val _messages = MutableStateFlow(initialMessages)
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    fun sendMessage(text: String) {
        val newMsg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}",
            senderId = "user_1",
            senderName = "You",
            senderInitials = "ME",
            messageText = text,
            timestamp = "Just now",
            isUser = true
        )
        _messages.value = _messages.value + newMsg
    }
}
