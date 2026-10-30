package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.model.ChatMessage
import com.neighborhero.neighborhero.data.network.NeighborHeroApiService
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.dto.ChatMessageDto
import com.neighborhero.neighborhero.data.network.dto.SendMessageDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

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
            isUser = false,
            conversationId = "conv_test"
        ),
        ChatMessage(
            id = "msg_2",
            senderId = "user_1",
            senderName = "You",
            senderInitials = "ME",
            messageText = "Yes, I got the last two bottles! Also picked up the fresh bakery sourdough you requested.",
            timestamp = "10:28 AM",
            isUser = true,
            conversationId = "conv_test"
        ),
        ChatMessage(
            id = "msg_3",
            senderId = "eleanor_vance",
            senderName = "Eleanor Vance",
            senderInitials = "EV",
            messageText = "Oh you're a lifesaver! Here is the entry gate code just in case the front buzzer is acting up:",
            timestamp = "10:30 AM",
            isUser = false,
            conversationId = "conv_test",
            attachmentCode = "#4921",
            attachmentTitle = "Gate Code"
        )
    )

    private val _messages = MutableStateFlow(if (seedForTests) testSeedMessages else emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _activeConversationId = MutableStateFlow<String?>(if (seedForTests) "conv_test" else null)
    val activeConversationId: StateFlow<String?> = _activeConversationId.asStateFlow()

    private val _activeRecipientId = MutableStateFlow<String?>(null)
    val activeRecipientId: StateFlow<String?> = _activeRecipientId.asStateFlow()

    /**
     * Isolates chat state to the specified conversation or recipient.
     * Clears existing displayed messages immediately when switching conversations.
     */
    fun setActiveConversation(conversationId: String?, recipientId: String?) {
        val sameConv = conversationId != null && conversationId == _activeConversationId.value
        val sameRec = recipientId != null && recipientId == _activeRecipientId.value
        if (!sameConv && !sameRec) {
            _messages.value = emptyList()
        }
        _activeConversationId.value = conversationId
        _activeRecipientId.value = recipientId
    }

    /**
     * Adds an optimistic message locally with a unique client ID.
     * Prevents duplication by tracking the generated temporary ID.
     */
    fun addOptimisticMessage(text: String, conversationId: String? = null): String {
        val tempId = "client_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"
        val newMsg = ChatMessage(
            id = tempId,
            senderId = "me",
            senderName = "You",
            senderInitials = "ME",
            messageText = text.trim(),
            timestamp = "Sending...",
            isUser = true,
            conversationId = conversationId ?: _activeConversationId.value
        )
        _messages.value = _messages.value + newMsg
        return tempId
    }

    fun sendMessage(text: String): String {
        return addOptimisticMessage(text)
    }

    suspend fun fetchMessages(
        conversationId: String? = null,
        recipientId: String? = null
    ): Result<List<ChatMessage>> {
        val effConvId = conversationId ?: _activeConversationId.value
        val effRecId = recipientId ?: _activeRecipientId.value

        if (effConvId.isNullOrBlank() && effRecId.isNullOrBlank()) {
            return Result.success(emptyList())
        }

        return try {
            val response = apiService.getChatMessages(
                conversationId = effConvId,
                recipientId = effRecId
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val dtos = response.body()!!.data ?: emptyList()
                val mapped = dtos.map { mapDtoToDomain(it) }

                // Reconcile: retain any optimistic in-flight messages that the server hasn't returned yet
                val inFlight = _messages.value.filter { it.id.startsWith("client_") }
                val combined = (mapped + inFlight).distinctBy { it.id }
                _messages.value = combined
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
        recipientId: String? = null,
        tempId: String? = null
    ): Result<ChatMessage> {
        val effConvId = conversationId ?: _activeConversationId.value
        val effRecId = recipientId ?: _activeRecipientId.value
        val clientMessageId = tempId ?: "client_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}"

        // Ensure optimistic entry exists
        if (_messages.value.none { it.id == clientMessageId }) {
            val optimistic = ChatMessage(
                id = clientMessageId,
                senderId = "me",
                senderName = "You",
                senderInitials = "ME",
                messageText = text.trim(),
                timestamp = "Sending...",
                isUser = true,
                conversationId = effConvId
            )
            _messages.value = _messages.value + optimistic
        }

        return try {
            val dto = SendMessageDto(
                messageText = text.trim(),
                conversationId = effConvId,
                recipientId = effRecId,
                clientMessageId = clientMessageId
            )
            val response = apiService.sendMessage(dto)
            if (response.isSuccessful && response.body()?.success == true) {
                val serverMsg = mapDtoToDomain(response.body()!!.data!!)

                // Reconcile: Replace temporary client message with server response smoothly
                val current = _messages.value
                val updated = current.map { if (it.id == clientMessageId) serverMsg else it }
                _messages.value = if (updated.any { it.id == serverMsg.id }) {
                    updated.distinctBy { it.id }
                } else {
                    (updated.filter { it.id != clientMessageId } + serverMsg).distinctBy { it.id }
                }

                if (!serverMsg.conversationId.isNullOrBlank()) {
                    _activeConversationId.value = serverMsg.conversationId
                }

                Result.success(serverMsg)
            } else {
                val msg = response.body()?.message ?: "Failed to send message"
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fully purges all chat state upon user logout or session switch.
     */
    fun clear() {
        _messages.value = emptyList()
        _activeConversationId.value = null
        _activeRecipientId.value = null
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
            conversationId = dto.conversationId,
            attachmentCode = dto.attachmentCode,
            attachmentTitle = dto.attachmentTitle
        )
    }
}
