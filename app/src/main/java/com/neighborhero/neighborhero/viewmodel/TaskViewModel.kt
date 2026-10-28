package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.dto.SubmitCompletionDto
import com.neighborhero.neighborhero.data.network.dto.VerifyCompletionDto
import com.neighborhero.neighborhero.data.repository.HelpRequestRepository
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import com.neighborhero.neighborhero.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TaskViewModel(
    private val requestRepository: HelpRequestRepository = RepositoryProvider.helpRequestRepository,
    private val userRepository: UserRepository = RepositoryProvider.userRepository
) : ViewModel() {
    private val _taskStarted = MutableStateFlow(false)
    val taskStarted: StateFlow<Boolean> = _taskStarted.asStateFlow()

    private val _taskSubmitted = MutableStateFlow(false)
    val taskSubmitted: StateFlow<Boolean> = _taskSubmitted.asStateFlow()

    private val _taskVerified = MutableStateFlow(false)
    val taskVerified: StateFlow<Boolean> = _taskVerified.asStateFlow()

    fun confirmStart(taskId: String? = null) {
        _taskStarted.value = true
        if (taskId != null) {
            requestRepository.updateRequestStatus(taskId, RequestStatus.IN_PROGRESS)
            viewModelScope.launch {
                try {
                    NetworkClient.getService().confirmTaskStart(taskId)
                } catch (_: Exception) {}
            }
        }
    }

    fun submitCompletion(taskId: String? = null, notes: String = "") {
        _taskSubmitted.value = true
        if (taskId != null) {
            requestRepository.updateRequestStatus(taskId, RequestStatus.PENDING_VERIFICATION)
            viewModelScope.launch {
                try {
                    NetworkClient.getService().submitCompletion(
                        taskId,
                        SubmitCompletionDto(notes = notes.ifBlank { "Task completed as requested." })
                    )
                } catch (_: Exception) {}
            }
        }
    }

    fun verifyCompletion(taskId: String? = null, rating: Int = 5, feedback: String = "") {
        _taskVerified.value = true
        if (taskId != null) {
            requestRepository.updateRequestStatus(taskId, RequestStatus.COMPLETED)
            viewModelScope.launch {
                try {
                    NetworkClient.getService().verifyCompletion(
                        taskId,
                        VerifyCompletionDto(rating = rating, feedback = feedback)
                    )
                } catch (_: Exception) {}
            }
        }
        userRepository.addPoints(150)
    }
}
