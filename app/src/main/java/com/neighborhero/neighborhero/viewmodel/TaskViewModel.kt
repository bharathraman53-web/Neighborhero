package com.neighborhero.neighborhero.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.RoadRouteResult
import com.neighborhero.neighborhero.data.network.RoadRoutingService
import com.neighborhero.neighborhero.data.network.dto.LiveTrackingDto
import com.neighborhero.neighborhero.data.network.dto.SubmitCompletionDto
import com.neighborhero.neighborhero.data.network.dto.TaskEvidenceDto
import com.neighborhero.neighborhero.data.network.dto.UpdateLiveLocationDto
import com.neighborhero.neighborhero.data.network.dto.VerifyCompletionDto
import com.neighborhero.neighborhero.data.repository.HelpRequestRepository
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import com.neighborhero.neighborhero.data.repository.UserRepository
import com.neighborhero.neighborhero.service.HelperLocationService
import com.neighborhero.neighborhero.util.LocationHelper
import org.osmdroid.util.GeoPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
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
                    stopHelperLocationSharing(taskId)
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
                    stopHelperLocationSharing(taskId)
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
            stopHelperLocationSharing(taskId)
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

    // --- Live Helper Tracking State & Methods ---
    private val _liveTracking = MutableStateFlow<LiveTrackingDto?>(null)
    val liveTracking: StateFlow<LiveTrackingDto?> = _liveTracking.asStateFlow()

    // --- Road-Based Routing (OpenStreetMap / OSRM) ---
    private val roadRoutingService = RoadRoutingService.getInstance()
    private val _roadRoute = MutableStateFlow<RoadRouteResult?>(null)
    val roadRoute: StateFlow<RoadRouteResult?> = _roadRoute.asStateFlow()

    private val _isRoutingLoading = MutableStateFlow(false)
    val isRoutingLoading: StateFlow<Boolean> = _isRoutingLoading.asStateFlow()

    private val _isSharingLocation = MutableStateFlow(false)
    val isSharingLocation: StateFlow<Boolean> = _isSharingLocation.asStateFlow()

    private val _trackingError = MutableStateFlow<String?>(null)
    val trackingError: StateFlow<String?> = _trackingError.asStateFlow()

    private var helperLocationJob: Job? = null
    private var requesterPollingJob: Job? = null
    private var routingJob: Job? = null

    fun clearTrackingError() {
        _trackingError.value = null
    }

    /**
     * Triggers road route calculation if helper moved >= 25m or on first fetch.
     * Prevents unnecessary API calls during continuous 3-second GPS updates.
     */
    fun updateRoadRouteIfNecessary(
        helperLat: Double,
        helperLng: Double,
        destLat: Double,
        destLng: Double,
        forceRefresh: Boolean = false
    ) {
        val origin = GeoPoint(helperLat, helperLng)
        val dest = GeoPoint(destLat, destLng)

        if (forceRefresh || roadRoutingService.shouldRecalculate(origin, dest) || _roadRoute.value == null) {
            routingJob?.cancel()
            routingJob = viewModelScope.launch {
                _isRoutingLoading.value = true
                val result = roadRoutingService.fetchRoadRoute(helperLat, helperLng, destLat, destLng, forceRefresh)
                if (result.isSuccess) {
                    _roadRoute.value = result.getOrNull()
                }
                _isRoutingLoading.value = false
            }
        }
    }

    /**
     * Helper Workflow: Start sharing location securely with requester for this active task
     * via continuous Android Foreground Service HelperLocationService.
     */
    fun startHelperLocationSharing(context: Context, taskId: String) {
        if (!LocationHelper.hasLocationPermission(context)) {
            _trackingError.value = "Location permission is required to share your live location."
            return
        }
        if (!LocationHelper.isLocationServiceEnabled(context)) {
            _trackingError.value = "GPS / Location service is turned off. Please enable GPS."
            return
        }

        _isSharingLocation.value = true
        _trackingError.value = null

        HelperLocationService.startTracking(context, taskId)

        // Observe tracking updates from HelperLocationService
        helperLocationJob?.cancel()
        helperLocationJob = viewModelScope.launch {
            HelperLocationService.latestTracking.collect { tracking ->
                if (tracking != null) {
                    _liveTracking.value = tracking
                }
            }
        }
    }

    fun stopHelperLocationSharing(taskId: String) {
        helperLocationJob?.cancel()
        helperLocationJob = null
        _isSharingLocation.value = false
        viewModelScope.launch {
            try {
                NetworkClient.getService().stopLiveLocation(taskId)
            } catch (_: Exception) {}
        }
    }

    fun stopHelperLocationSharing(context: Context, taskId: String) {
        helperLocationJob?.cancel()
        helperLocationJob = null
        _isSharingLocation.value = false
        HelperLocationService.stopTracking(context)
        viewModelScope.launch {
            try {
                NetworkClient.getService().stopLiveLocation(taskId)
            } catch (_: Exception) {}
        }
    }

    /**
     * Requester Workflow: Start live polling to track helper's location during active task.
     */
    fun fetchLiveTracking(taskId: String) {
        viewModelScope.launch {
            try {
                val resp = NetworkClient.getService().getLiveLocation(taskId)
                if (resp.isSuccessful && resp.body()?.success == true) {
                    val tracking = resp.body()!!.data
                    _liveTracking.value = tracking
                    if (tracking?.isActive == true && tracking.latitude != null && tracking.longitude != null) {
                        val req = requestRepository.getRequestById(taskId)
                        val destLat: Double = tracking.requesterLatitude?.takeIf { it != 0.0 }
                            ?: req?.latitude?.takeIf { it != 0.0 }
                            ?: 13.0827
                        val destLng: Double = tracking.requesterLongitude?.takeIf { it != 0.0 }
                            ?: req?.longitude?.takeIf { it != 0.0 }
                            ?: 80.2707
                        updateRoadRouteIfNecessary(tracking.latitude, tracking.longitude, destLat, destLng)
                    }
                } else {
                    val err = resp.body()?.message ?: "Unable to fetch helper location"
                    _trackingError.value = err
                }
            } catch (e: Exception) {
                _trackingError.value = e.message
            }
        }
    }

    fun startTrackingPolling(taskId: String) {
        requesterPollingJob?.cancel()
        requesterPollingJob = viewModelScope.launch {
            while (isActive) {
                try {
                    val resp = NetworkClient.getService().getLiveLocation(taskId)
                    if (resp.isSuccessful && resp.body()?.success == true) {
                        val tracking = resp.body()!!.data
                        _liveTracking.value = tracking
                        if (tracking?.isActive == true && tracking.latitude != null && tracking.longitude != null) {
                            val req = requestRepository.getRequestById(taskId)
                            val destLat: Double = tracking.requesterLatitude?.takeIf { it != 0.0 }
                                ?: req?.latitude?.takeIf { it != 0.0 }
                                ?: 13.0827
                            val destLng: Double = tracking.requesterLongitude?.takeIf { it != 0.0 }
                                ?: req?.longitude?.takeIf { it != 0.0 }
                                ?: 80.2707
                            updateRoadRouteIfNecessary(tracking.latitude, tracking.longitude, destLat, destLng)
                        } else if (tracking?.isActive == false) {
                            _roadRoute.value = null
                            break
                        }
                    }
                } catch (_: Exception) {}
                delay(3000L) // Real-time 3-second polling while tracking screen is active
            }
        }
    }

    fun stopTrackingPolling() {
        requesterPollingJob?.cancel()
        requesterPollingJob = null
        routingJob?.cancel()
        routingJob = null
        roadRoutingService.clearCache()
        _roadRoute.value = null
    }

    override fun onCleared() {
        super.onCleared()
        helperLocationJob?.cancel()
        requesterPollingJob?.cancel()
        routingJob?.cancel()
        roadRoutingService.clearCache()
        _roadRoute.value = null
    }
}
