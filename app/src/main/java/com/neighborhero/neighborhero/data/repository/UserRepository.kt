package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserRepository {
    private val _currentUser = MutableStateFlow(User())
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    fun updateUserProfile(name: String, neighborhood: String, bio: String, skills: List<String>) {
        val updated = _currentUser.value.copy(
            fullName = name,
            neighborhood = neighborhood,
            bio = bio,
            skills = skills
        )
        _currentUser.value = updated
    }
}
