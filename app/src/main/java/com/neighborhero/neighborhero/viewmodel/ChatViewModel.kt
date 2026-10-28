package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.model.ChatMessage
import com.neighborhero.neighborhero.data.repository.ChatRepository
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = RepositoryProvider.chatRepository
) : ViewModel() {
    val messages: StateFlow<List<ChatMessage>> = repository.messages

    private var pollingJob: Job? = null

    init {
        refreshMessages()
    }

    fun refreshMessages(conversationId: String? = null) {
        viewModelScope.launch {
            repository.fetchMessages(conversationId)
        }
    }

    fun startPolling(conversationId: String? = null, intervalMs: Long = 4000L) {
        stopPolling()
        pollingJob = viewModelScope.launch {
            while (isActive) {
                repository.fetchMessages(conversationId)
                delay(intervalMs)
            }
        }
    }

    fun stopPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    fun sendMessage(text: String, conversationId: String? = null, recipientId: String? = null) {
        if (text.isNotBlank()) {
            repository.sendMessage(text)
            viewModelScope.launch {
                repository.sendRemoteMessage(text, conversationId, recipientId)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
