package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.data.model.NotificationItem
import com.neighborhero.neighborhero.data.network.SessionManager
import com.neighborhero.neighborhero.data.repository.HelpRequestRepository
import com.neighborhero.neighborhero.data.repository.HelperRepository
import com.neighborhero.neighborhero.data.repository.NotificationRepository
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import com.neighborhero.neighborhero.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository = RepositoryProvider.userRepository,
    private val helpRequestRepository: HelpRequestRepository = RepositoryProvider.helpRequestRepository,
    private val helperRepository: HelperRepository = RepositoryProvider.helperRepository,
    private val notificationRepository: NotificationRepository = RepositoryProvider.notificationRepository,
    private val sessionManager: SessionManager = SessionManager.getInstance()
) : ViewModel() {
    val currentUser = userRepository.currentUser
    val nearbyRequests: StateFlow<List<HelpRequest>> = helpRequestRepository.requests
    val topHelpers: StateFlow<List<HelperProfile>> = helperRepository.helpers
    val notifications: StateFlow<List<NotificationItem>> = notificationRepository.notifications

    private val _selectedLocation = MutableStateFlow(
        sessionManager.getSelectedLocation()?.takeIf { it.isNotBlank() }
            ?: currentUser.value.neighborhood.ifBlank { "Maplewood District" }
    )
    val selectedLocation: StateFlow<String> = _selectedLocation.asStateFlow()

    private var pollJob: Job? = null

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            userRepository.fetchProfile()
            val savedLocation = sessionManager.getSelectedLocation()
            if (!savedLocation.isNullOrBlank()) {
                _selectedLocation.value = savedLocation
            } else if (currentUser.value.neighborhood.isNotBlank()) {
                _selectedLocation.value = currentUser.value.neighborhood
            }
            helpRequestRepository.fetchRequests()
            helperRepository.fetchHelpers()
            notificationRepository.fetchNotifications()
        }
    }

    fun startPolling(intervalMs: Long = 10000L) {
        stopPolling()
        pollJob = viewModelScope.launch {
            while (isActive) {
                delay(intervalMs)
                helpRequestRepository.fetchRequests()
                notificationRepository.fetchNotifications()
            }
        }
    }

    fun stopPolling() {
        pollJob?.cancel()
        pollJob = null
    }

    fun markNotificationAsRead(id: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }

    fun markAllNotificationsAsRead() {
        viewModelScope.launch {
            notificationRepository.markAllAsRead()
        }
    }

    fun clearAllNotifications() {
        viewModelScope.launch {
            notificationRepository.clearAllNotifications()
        }
    }

    fun updateSelectedLocation(newLocation: String, lat: Double? = null, lng: Double? = null) {
        val cleanLocation = newLocation.trim().ifBlank { "Maplewood District" }
        _selectedLocation.value = cleanLocation
        sessionManager.saveLocation(cleanLocation, lat, lng)
        userRepository.updateUserProfile(
            name = currentUser.value.fullName,
            neighborhood = cleanLocation,
            bio = currentUser.value.bio,
            skills = currentUser.value.skills,
            phone = currentUser.value.phone
        )
        viewModelScope.launch {
            userRepository.updateRemoteProfile(
                name = currentUser.value.fullName,
                neighborhood = cleanLocation,
                bio = currentUser.value.bio,
                phone = currentUser.value.phone
            )
            helpRequestRepository.fetchRequests()
            helperRepository.fetchHelpers()
        }
    }

    fun updateUserProfile(name: String, neighborhood: String, bio: String, skills: List<String> = emptyList(), phone: String? = null) {
        val cleanNeighborhood = neighborhood.trim()
        if (cleanNeighborhood.isNotBlank()) {
            _selectedLocation.value = cleanNeighborhood
            sessionManager.saveLocation(cleanNeighborhood)
        }
        userRepository.updateUserProfile(
            name = name,
            neighborhood = neighborhood,
            bio = bio,
            skills = if (skills.isEmpty()) currentUser.value.skills else skills,
            phone = phone
        )
        viewModelScope.launch {
            userRepository.updateRemoteProfile(name, neighborhood, bio, phone)
        }
    }

    fun uploadProfilePhoto(
        bytes: ByteArray,
        mimeType: String = "image/jpeg",
        fileName: String = "avatar.jpg",
        onComplete: (Boolean, String?) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = userRepository.uploadProfilePhoto(bytes, mimeType, fileName)
            if (result.isSuccess) {
                onComplete(true, null)
            } else {
                onComplete(false, result.exceptionOrNull()?.message ?: "Upload failed")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
    }
}
