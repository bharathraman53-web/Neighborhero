package com.neighborhero.neighborhero.data.network.dto

import com.google.gson.annotations.SerializedName

data class SendMessageDto(
    @SerializedName("message_text") val messageText: String,
    @SerializedName("conversation_id") val conversationId: String? = null,
    @SerializedName("recipient_id") val recipientId: String? = null,
    @SerializedName("attachment_code") val attachmentCode: String? = null,
    @SerializedName("attachment_title") val attachmentTitle: String? = null
)

data class ChatMessageDto(
    @SerializedName("id") val id: String,
    @SerializedName("sender_id") val senderId: String,
    @SerializedName("sender_name") val senderName: String,
    @SerializedName("sender_initials") val senderInitials: String = "NH",
    @SerializedName("message_text") val messageText: String,
    @SerializedName("timestamp") val timestamp: String,
    @SerializedName("is_user") val isUser: Boolean,
    @SerializedName("attachment_code") val attachmentCode: String? = null,
    @SerializedName("attachment_title") val attachmentTitle: String? = null
)

