package com.neighborhero.neighborhero.data.model

data class ChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val senderInitials: String,
    val messageText: String,
    val timestamp: String,
    val isUser: Boolean,
    val attachmentCode: String? = null,
    val attachmentTitle: String? = null
)
