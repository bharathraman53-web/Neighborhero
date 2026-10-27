package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import com.neighborhero.neighborhero.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AuthViewModel(private val userRepository: UserRepository = UserRepository()) : ViewModel() {
    private val _isLoggedIn = MutableStateFlow(true)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _onboardingStep = MutableStateFlow(1)
    val onboardingStep: StateFlow<Int> = _onboardingStep.asStateFlow()

    fun nextOnboardingStep() {
        if (_onboardingStep.value < 3) {
            _onboardingStep.value += 1
        }
    }

    fun login(emailOrPhone: String, pass: String) {
        _isLoggedIn.value = true
    }

    fun logout() {
        _isLoggedIn.value = false
    }
}
