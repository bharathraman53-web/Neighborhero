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

    private val _isAccepting = MutableStateFlow(false)
    val isAccepting: StateFlow<Boolean> = _isAccepting.asStateFlow()

    private val _acceptError = MutableStateFlow<String?>(null)
    val acceptError: StateFlow<String?> = _acceptError.asStateFlow()

    private val _acceptSuccess = MutableStateFlow<String?>(null)
    val acceptSuccess: StateFlow<String?> = _acceptSuccess.asStateFlow()

    private val _isCreating = MutableStateFlow(false)
    val isCreating: StateFlow<Boolean> = _isCreating.asStateFlow()

    private val _createError = MutableStateFlow<String?>(null)
    val createError: StateFlow<String?> = _createError.asStateFlow()

    init {
        refreshRequests()
    }

    fun clearAcceptStatus() {
        _acceptError.value = null
        _acceptSuccess.value = null
    }

    fun clearCreateStatus() {
        _createError.value = null
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
        location: String,
        assignedHelperId: String? = null,
        assignedHelperName: String? = null,
        latitude: Double? = null,
        longitude: Double? = null,
        onDone: (Boolean, HelpRequest?, String?) -> Unit = { _, _, _ -> }
    ): String {
        if (_isCreating.value) return ""
        _isCreating.value = true
        _createError.value = null

        val currentUser = RepositoryProvider.userRepository.currentUser.value
        val newId = "req_${System.currentTimeMillis()}"
        val initialStatus = if (!assignedHelperId.isNullOrBlank()) RequestStatus.IN_PROGRESS else RequestStatus.OPEN
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
            status = initialStatus,
            assignedHelperId = assignedHelperId,
            assignedHelperName = assignedHelperName,
            requesterId = currentUser.id.ifBlank { null },
            requesterAvatarUrl = currentUser.avatarUrl.ifBlank { null },
            latitude = latitude,
            longitude = longitude
        )
        try {
            repository.addRequest(newReq)
        } catch (e: Exception) {
            android.util.Log.e("HelpRequestViewModel", "Failed to add request locally: ${e.message}")
        }

        // Asynchronously persist to MySQL backend
        viewModelScope.launch {
            try {
                val result = repository.createRemoteRequest(
                    title = title,
                    category = category,
                    description = description,
                    time = time,
                    location = location,
                    tempId = newId,
                    assignedHelperId = assignedHelperId,
                    latitude = latitude,
                    longitude = longitude
                )
                if (result.isSuccess) {
                    val created = result.getOrNull() ?: newReq
                    repository.fetchRequests()
                    _isCreating.value = false
                    onDone(true, created, null)
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Failed to save request to server"
                    _createError.value = err
                    _isCreating.value = false
                    onDone(false, null, err)
                }
            } catch (e: Exception) {
                val err = e.message ?: "Failed to create request"
                _createError.value = err
                _isCreating.value = false
                onDone(false, null, err)
            }
        }

        return newId
    }

    fun acceptRequest(
        requestId: String,
        helperName: String,
        onDone: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (_isAccepting.value) return // Prevent duplicate concurrent requests
        _isAccepting.value = true
        _acceptError.value = null
        _acceptSuccess.value = null

        viewModelScope.launch {
            try {
                val result = repository.assignRemoteHelper(requestId)
                if (result.isSuccess) {
                    val updated = result.getOrNull()
                    // Update local repository and cache only after remote backend confirms acceptance
                    repository.updateRequestStatus(
                        requestId,
                        RequestStatus.IN_PROGRESS,
                        updated?.assignedHelperName ?: helperName
                    )
                    _acceptSuccess.value = "Task accepted successfully! You are assigned to this task."
                    // Refresh requests to update helper's lists and nearby cache
                    repository.fetchRequests()
                    onDone(true, null)
                } else {
                    val err = result.exceptionOrNull()?.message ?: "Failed to accept task"
                    _acceptError.value = err
                    // Refresh remote state to reflect current assignment if taken by another helper
                    repository.fetchRequests()
                    onDone(false, err)
                }
            } catch (e: Exception) {
                val err = e.message ?: "Failed to accept task"
                _acceptError.value = err
                onDone(false, err)
            } finally {
                _isAccepting.value = false
            }
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
