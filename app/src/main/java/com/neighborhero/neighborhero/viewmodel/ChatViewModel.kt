package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.model.ChatMessage
import com.neighborhero.neighborhero.data.repository.ChatRepository
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = RepositoryProvider.chatRepository
) : ViewModel() {
    val messages: StateFlow<List<ChatMessage>> = repository.messages
    val activeConversationId: StateFlow<String?> = repository.activeConversationId
    val activeRecipientId: StateFlow<String?> = repository.activeRecipientId

    private val _recipientName = MutableStateFlow<String?>("Neighbor Chat")
    val recipientName: StateFlow<String?> = _recipientName.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private var pollingJob: Job? = null

    /**
     * Scopes chat to a specific conversation or recipient.
     * Clears prior conversation state so no messages leak across sessions or screens.
     */
    fun openConversation(
        conversationId: String? = null,
        recipientId: String? = null,
        partnerName: String? = null
    ) {
        _recipientName.value = partnerName ?: "Neighbor Chat"
        repository.setActiveConversation(conversationId, recipientId)
        startPolling(conversationId, recipientId)
    }

    fun refreshMessages(conversationId: String? = null, recipientId: String? = null) {
        viewModelScope.launch {
            repository.fetchMessages(conversationId, recipientId)
        }
    }

    fun startPolling(
        conversationId: String? = null,
        recipientId: String? = null,
        intervalMs: Long = 4000L
    ) {
        stopPolling()
        // Immediately fetch once
        refreshMessages(conversationId, recipientId)
        pollingJob = viewModelScope.launch {
            while (isActive) {
                delay(intervalMs)
                repository.fetchMessages(conversationId, recipientId)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    /**
     * Submits a message with guard against double-taps and duplicate submissions.
     */
    fun sendMessage(
        text: String,
        conversationId: String? = null,
        recipientId: String? = null
    ) {
        val trimmed = text.trim()
        if (trimmed.isBlank() || _isSending.value) return

        _isSending.value = true
        val tempId = repository.addOptimisticMessage(trimmed, conversationId)

        viewModelScope.launch {
            try {
                repository.sendRemoteMessage(
                    text = trimmed,
                    conversationId = conversationId,
                    recipientId = recipientId,
                    tempId = tempId
                )
            } finally {
                _isSending.value = false
            }
        }
    }

    fun clearSession() {
        stopPolling()
        _recipientName.value = "Neighbor Chat"
        _isSending.value = false
        repository.clear()
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
