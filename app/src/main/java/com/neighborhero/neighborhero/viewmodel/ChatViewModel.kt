package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import com.neighborhero.neighborhero.data.model.ChatMessage
import com.neighborhero.neighborhero.data.repository.ChatRepository
import kotlinx.coroutines.flow.StateFlow

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {
    val messages: StateFlow<List<ChatMessage>> = repository.messages

    fun sendMessage(text: String) {
        if (text.isNotBlank()) {
            repository.sendMessage(text)
        }
    }
}
