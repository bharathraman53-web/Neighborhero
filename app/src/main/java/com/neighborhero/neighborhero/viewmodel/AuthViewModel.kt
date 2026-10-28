package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neighborhero.neighborhero.data.network.SessionManager
import com.neighborhero.neighborhero.data.repository.RepositoryProvider
import com.neighborhero.neighborhero.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val userRepository: UserRepository = RepositoryProvider.userRepository,
    private val sessionManager: SessionManager = SessionManager.getInstance()
) : ViewModel() {

    private val _isLoggedIn = MutableStateFlow(sessionManager.isLoggedIn())
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    private val _onboardingStep = MutableStateFlow(1)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    fun nextOnboardingStep() {
        if (_onboardingStep.value < 3) {
            _onboardingStep.value += 1
        }
    }

    fun login(emailOrPhone: String, pass: String) {
        if (emailOrPhone.isBlank() || pass.isBlank()) {
            _authError.value = "Please enter both credentials."
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = userRepository.login(emailOrPhone, pass)
            if (result.isSuccess) {
                _isLoggedIn.value = true
            } else {
                _authError.value = result.exceptionOrNull()?.message ?: "Login failed."
            }
            _isLoading.value = false
        }
    }

    fun register(
        fullName: String,
        email: String,
        pass: String,
        phone: String? = null,
        neighborhood: String? = null,
        bio: String? = null,
        skills: List<String>? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _authError.value = null
            val result = userRepository.register(fullName, email, pass, phone, neighborhood, bio, skills)
            if (result.isSuccess) {
                _isLoggedIn.value = true
                onSuccess()
            } else {
                _authError.value = result.exceptionOrNull()?.message ?: "Registration failed."
            }
            _isLoading.value = false
        }
    }

    fun logout() {
        userRepository.logout()
        _isLoggedIn.value = false
    }

    fun clearError() {
        _authError.value = null
    }
}
