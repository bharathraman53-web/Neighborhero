package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.dto.SubmitCompletionDto
import com.neighborhero.neighborhero.data.network.dto.TaskEvidenceDto
import com.neighborhero.neighborhero.data.network.dto.VerifyCompletionDto
import com.neighborhero.neighborhero.data.repository.HelpRequestRepository
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import com.neighborhero.neighborhero.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

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

    private val _currentEvidence = MutableStateFlow<TaskEvidenceDto?>(null)
    val currentEvidence: StateFlow<TaskEvidenceDto?> = _currentEvidence.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _evidenceError = MutableStateFlow<String?>(null)
    val evidenceError: StateFlow<String?> = _evidenceError.asStateFlow()

    fun clearError() {
        _evidenceError.value = null
    }

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

    fun submitCompletion(
        taskId: String? = null,
        notes: String = "",
        onDone: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (taskId == null) {
            onDone(false, "Invalid task ID")
            return
        }
        if (_isSubmitting.value) {
            return
        }
        _isSubmitting.value = true
        _evidenceError.value = null
        _taskSubmitted.value = true
        requestRepository.updateRequestStatus(taskId, RequestStatus.PENDING_VERIFICATION)
        viewModelScope.launch {
            try {
                val response = NetworkClient.getService().submitCompletion(
                    taskId,
                    SubmitCompletionDto(
                        notes = notes.ifBlank { "Task completed as requested." }
                    )
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    onDone(true, null)
                } else {
                    _taskSubmitted.value = false
                    requestRepository.updateRequestStatus(taskId, RequestStatus.IN_PROGRESS)
                    val rawError = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    var err = response.body()?.message
                    if (err.isNullOrBlank() && !rawError.isNullOrBlank()) {
                        try {
                            val json = org.json.JSONObject(rawError)
                            if (json.has("message")) {
                                err = json.getString("message")
                            }
                        } catch (_: Exception) {}
                    }
                    if (err.isNullOrBlank()) {
                        err = when (response.code()) {
                            403 -> "Forbidden: Only the assigned helper can submit task completion"
                            409 -> "Task completion has already been submitted and is pending verification"
                            else -> "Submission failed (HTTP ${response.code()})"
                        }
                    }
                    _evidenceError.value = err
                    onDone(false, err)
                }
            } catch (e: Exception) {
                onDone(true, null)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun submitCompletionWithPhoto(
        taskId: String? = null,
        notes: String = "",
        photoBytes: ByteArray? = null,
        mimeType: String = "image/jpeg",
        fileName: String = "evidence.jpg",
        onDone: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        if (taskId == null) {
            onDone(false, "Invalid task ID")
            return
        }
        if (_isSubmitting.value) {
            return
        }
        _isSubmitting.value = true
        _evidenceError.value = null
        viewModelScope.launch {
            try {
                var photoUrl: String? = null
                if (photoBytes != null && photoBytes.isNotEmpty()) {
                    val mediaType = mimeType.toMediaTypeOrNull()
                    val reqBody = photoBytes.toRequestBody(mediaType)
                    val part = MultipartBody.Part.createFormData("photo", fileName, reqBody)
                    val uploadRes = NetworkClient.getService().uploadEvidencePhoto(taskId, part)
                    if (uploadRes.isSuccessful && uploadRes.body()?.success == true) {
                        photoUrl = uploadRes.body()!!.data?.photoUrl
                    } else {
                        val rawError = try { uploadRes.errorBody()?.string() } catch (_: Exception) { null }
                        var err = uploadRes.body()?.message
                        if (err.isNullOrBlank() && !rawError.isNullOrBlank()) {
                            try {
                                val json = org.json.JSONObject(rawError)
                                if (json.has("message")) {
                                    err = json.getString("message")
                                }
                            } catch (_: Exception) {}
                        }
                        if (err.isNullOrBlank()) {
                            err = when (uploadRes.code()) {
                                403 -> "Forbidden: Only the assigned helper can upload completion evidence"
                                409 -> "Task completion has already been submitted and is pending verification"
                                else -> "Failed to upload proof photo (HTTP ${uploadRes.code()})"
                            }
                        }
                        _evidenceError.value = err
                        onDone(false, err)
                        return@launch
                    }
                }

                val response = NetworkClient.getService().submitCompletion(
                    taskId,
                    SubmitCompletionDto(
                        notes = notes.ifBlank { "Task completed as requested." },
                        proofPhotoUrl = photoUrl
                    )
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    _taskSubmitted.value = true
                    requestRepository.updateRequestStatus(taskId, RequestStatus.PENDING_VERIFICATION)
                    onDone(true, null)
                } else {
                    val rawError = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    var err = response.body()?.message
                    if (err.isNullOrBlank() && !rawError.isNullOrBlank()) {
                        try {
                            val json = org.json.JSONObject(rawError)
                            if (json.has("message")) {
                                err = json.getString("message")
                            }
                        } catch (_: Exception) {}
                    }
                    if (err.isNullOrBlank()) {
                        err = when (response.code()) {
                            403 -> "Forbidden: Only the assigned helper can submit task completion"
                            409 -> "Task completion has already been submitted and is pending verification"
                            else -> "Submission failed (HTTP ${response.code()})"
                        }
                    }
                    _evidenceError.value = err
                    onDone(false, err)
                }
            } catch (e: Exception) {
                if (photoBytes == null || photoBytes.isEmpty()) {
                    _taskSubmitted.value = true
                    requestRepository.updateRequestStatus(taskId, RequestStatus.PENDING_VERIFICATION)
                    onDone(true, null)
                } else {
                    val err = e.message ?: "Failed to submit completion"
                    _evidenceError.value = err
                    onDone(false, err)
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun fetchEvidence(taskId: String) {
        viewModelScope.launch {
            try {
                val response = NetworkClient.getService().getTaskEvidence(taskId)
                if (response.isSuccessful && response.body()?.success == true) {
                    _currentEvidence.value = response.body()!!.data
                }
            } catch (_: Exception) {}
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
