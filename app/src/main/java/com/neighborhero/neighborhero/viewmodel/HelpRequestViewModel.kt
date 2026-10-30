package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.repository.HelpRequestRepository
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HelpRequestViewModel(
    private val repository: HelpRequestRepository = RepositoryProvider.helpRequestRepository
) : ViewModel() {
    val requests: StateFlow<List<HelpRequest>> = repository.requests
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val errorMessage: StateFlow<String?> = repository.errorMessage

    private val _selectedRequestId = MutableStateFlow<String?>(null)
    val selectedRequestId: StateFlow<String?> = _selectedRequestId.asStateFlow()

    init {
        refreshRequests()
    }

    fun refreshRequests(category: String? = null) {
        viewModelScope.launch {
            repository.fetchRequests(category = category)
        }
    }

    fun selectRequest(requestId: String) {
        _selectedRequestId.value = requestId
    }

    fun getRequest(id: String?): HelpRequest? {
        if (id == null) return requests.value.firstOrNull()
        return repository.getRequestById(id) ?: requests.value.find { it.id == id }
    }

    fun createRequest(
        title: String,
        category: String,
        description: String,
        time: String,
        location: String
    ): String {
        val currentUser = RepositoryProvider.userRepository.currentUser.value
        val newId = "req_${System.currentTimeMillis()}"
        val newReq = HelpRequest(
            id = newId,
            title = title,
            category = category,
            description = description,
            requesterName = currentUser.fullName.ifBlank { "Community Neighbor" },
            requesterRating = if (currentUser.rating > 0f) currentUser.rating else 5.0f,
            requesterTaskCount = currentUser.completedTasksCount,
            location = location.ifBlank { currentUser.neighborhood.ifBlank { "Maplewood District" } },
            distance = "0.1 mi away",
            requestedTime = time.ifBlank { "Due Today, 4:00 PM" },
            isUrgent = false,
            status = RequestStatus.OPEN
        )
        repository.addRequest(newReq)

        // Asynchronously persist to MySQL backend
        viewModelScope.launch {
            repository.createRemoteRequest(
                title = title,
                category = category,
                description = description,
                time = time,
                location = location,
                tempId = newId
            )
        }

        return newId
    }

    fun acceptRequest(requestId: String, helperName: String) {
        repository.updateRequestStatus(requestId, RequestStatus.IN_PROGRESS, helperName)
        viewModelScope.launch {
            repository.assignRemoteHelper(requestId)
        }
    }

    fun submitCompletion(requestId: String) {
        repository.updateRequestStatus(requestId, RequestStatus.PENDING_VERIFICATION)
    }

    fun verifyCompletion(requestId: String) {
        repository.updateRequestStatus(requestId, RequestStatus.COMPLETED)
        RepositoryProvider.userRepository.addPoints(150)
    }
}
