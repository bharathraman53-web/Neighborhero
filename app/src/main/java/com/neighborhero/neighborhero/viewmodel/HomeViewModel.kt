package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.data.model.NotificationItem
import com.neighborhero.neighborhero.data.repository.HelpRequestRepository
import com.neighborhero.neighborhero.data.repository.HelperRepository
import com.neighborhero.neighborhero.data.repository.NotificationRepository
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import com.neighborhero.neighborhero.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class HomeViewModel(
    private val userRepository: UserRepository = RepositoryProvider.userRepository,
    private val helpRequestRepository: HelpRequestRepository = RepositoryProvider.helpRequestRepository,
    private val helperRepository: HelperRepository = RepositoryProvider.helperRepository,
    private val notificationRepository: NotificationRepository = RepositoryProvider.notificationRepository
) : ViewModel() {
    val currentUser = userRepository.currentUser
    val nearbyRequests: StateFlow<List<HelpRequest>> = helpRequestRepository.requests
    val topHelpers: StateFlow<List<HelperProfile>> = helperRepository.helpers
    val notifications: StateFlow<List<NotificationItem>> = notificationRepository.notifications

    private var pollJob: Job? = null

    init {
        refreshDashboard()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            userRepository.fetchProfile()
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

    fun updateUserProfile(name: String, neighborhood: String, bio: String, skills: List<String> = emptyList(), phone: String? = null) {
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
