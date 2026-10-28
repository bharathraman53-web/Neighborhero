package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.model.ChatMessage
import com.neighborhero.neighborhero.data.network.NeighborHeroApiService
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.dto.ChatMessageDto
import com.neighborhero.neighborhero.data.network.dto.SendMessageDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ChatRepository(
    private val apiService: NeighborHeroApiService = NetworkClient.getService(),
    seedForTests: Boolean = false
) {
    private val testSeedMessages = listOf(
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

    private val _messages = MutableStateFlow(if (seedForTests) testSeedMessages else emptyList())
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

    suspend fun fetchMessages(conversationId: String? = null): Result<List<ChatMessage>> {
        return try {
            val response = apiService.getChatMessages(conversationId = conversationId)
            if (response.isSuccessful && response.body()?.success == true) {
                val dtos = response.body()!!.data ?: emptyList()
                val mapped = dtos.map { mapDtoToDomain(it) }
                _messages.value = mapped
                Result.success(mapped)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch chat messages"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendRemoteMessage(
        text: String,
        conversationId: String? = null,
        recipientId: String? = null
    ): Result<ChatMessage> {
        return try {
            val dto = SendMessageDto(
                messageText = text,
                conversationId = conversationId,
                recipientId = recipientId
            )
            val response = apiService.sendMessage(dto)
            if (response.isSuccessful && response.body()?.success == true) {
                val msg = mapDtoToDomain(response.body()!!.data!!)
                _messages.value = _messages.value + msg
                Result.success(msg)
            } else {
                // Local fallback
                sendMessage(text)
                Result.success(_messages.value.last())
            }
        } catch (e: Exception) {
            sendMessage(text)
            Result.success(_messages.value.last())
        }
    }

    private fun mapDtoToDomain(dto: ChatMessageDto): ChatMessage {
        return ChatMessage(
            id = dto.id,
            senderId = dto.senderId,
            senderName = dto.senderName,
            senderInitials = dto.senderInitials,
            messageText = dto.messageText,
            timestamp = dto.timestamp,
            isUser = dto.isUser,
            attachmentCode = dto.attachmentCode,
            attachmentTitle = dto.attachmentTitle
        )
    }
}
