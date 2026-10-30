package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.model.User
import com.neighborhero.neighborhero.data.network.NeighborHeroApiService
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.SessionManager
import com.neighborhero.neighborhero.data.network.dto.LoginRequest
import com.neighborhero.neighborhero.data.network.dto.RegisterRequest
import com.neighborhero.neighborhero.data.network.dto.UpdateProfileRequest
import com.neighborhero.neighborhero.data.network.dto.UserDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

class UserRepository(
    private val apiService: NeighborHeroApiService = NetworkClient.getService(),
    private val sessionManager: SessionManager = SessionManager.getInstance(),
    seedForTests: Boolean = false
) {
    private val defaultUser = if (seedForTests) {
        User(
            id = "user_1",
            fullName = "Eleanor Vance",
            email = "eleanor@example.com",
            phone = "(555) 234-5678",
            neighborhood = "Maplewood & Oakridge",
            bio = "Passionate gardener and retired teacher who loves helping out with local grocery runs, pet sitting, and weekend tutoring.",
            skills = listOf("Groceries", "Repairs", "Pet Care", "Tutoring"),
            points = 350,
            completedTasksCount = 24,
            rating = 4.9f
        )
    } else {
        User(
            id = "",
            fullName = "",
            email = "",
            phone = "",
            neighborhood = "",
            bio = "",
            skills = emptyList(),
            points = 0,
            completedTasksCount = 0,
            rating = 5.0f,
            isVerified = false
        )
    }

    private val _currentUser = MutableStateFlow(defaultUser)
    val currentUser: StateFlow<User> = _currentUser.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun updateUserProfile(name: String, neighborhood: String, bio: String, skills: List<String>, phone: String? = null) {
        val updated = _currentUser.value.copy(
            fullName = name,
            neighborhood = neighborhood,
            bio = bio,
            skills = skills,
            phone = phone ?: _currentUser.value.phone
        )
        _currentUser.value = updated
    }

    fun addPoints(points: Int) {
        _currentUser.value = _currentUser.value.copy(
            points = _currentUser.value.points + points,
            completedTasksCount = _currentUser.value.completedTasksCount + 1
        )
    }

    suspend fun login(emailOrPhone: String, pass: String): Result<User> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val response = apiService.login(LoginRequest(emailOrPhone, pass))
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                sessionManager.saveToken(data.token)
                sessionManager.saveUserId(data.user.id)
                val user = mapUserDtoToDomain(data.user)
                _currentUser.value = user
                Result.success(user)
            } else {
                val msg = if (response.code() == 401) {
                    "Incorrect email or phone number, or password. Please try again."
                } else if (response.code() in 500..599) {
                    "Server error occurred. Please try again later."
                } else {
                    val rawError = try { response.errorBody()?.string() } catch (_: Exception) { null }
                    if (rawError != null && rawError.contains("Incorrect email or phone number")) {
                        "Incorrect email or phone number, or password. Please try again."
                    } else {
                        response.body()?.message ?: "Unable to sign in. Please verify your credentials and try again."
                    }
                }
                _errorMessage.value = msg
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            val userFriendlyMsg = when (e) {
                is java.net.ConnectException,
                is java.net.SocketTimeoutException,
                is java.net.UnknownHostException ->
                    "Unable to connect to the server. Please check your network connection and try again."
                else -> e.message ?: "An unexpected error occurred. Please try again."
            }
            _errorMessage.value = userFriendlyMsg
            Result.failure(Exception(userFriendlyMsg, e))
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun register(
        fullName: String,
        email: String,
        pass: String,
        phone: String? = null,
        neighborhood: String? = null,
        bio: String? = null,
        skills: List<String>? = null
    ): Result<User> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val response = apiService.register(
                RegisterRequest(fullName, email, phone, pass, neighborhood, bio, skills)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val data = response.body()!!.data!!
                sessionManager.saveToken(data.token)
                sessionManager.saveUserId(data.user.id)
                val user = mapUserDtoToDomain(data.user)
                _currentUser.value = user
                Result.success(user)
            } else {
                val msg = response.body()?.message ?: "Registration failed"
                _errorMessage.value = msg
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message ?: "Network error"
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun fetchProfile(): Result<User> {
        return try {
            val response = apiService.getCurrentUser()
            if (response.isSuccessful && response.body()?.success == true) {
                val user = mapUserDtoToDomain(response.body()!!.data!!)
                _currentUser.value = user
                Result.success(user)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch profile"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateRemoteProfile(name: String, neighborhood: String, bio: String, phone: String? = null): Result<User> {
        return try {
            val response = apiService.updateProfile(
                UpdateProfileRequest(fullName = name, neighborhood = neighborhood, bio = bio, phone = phone)
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val user = mapUserDtoToDomain(response.body()!!.data!!)
                _currentUser.value = user
                Result.success(user)
            } else {
                updateUserProfile(name, neighborhood, bio, _currentUser.value.skills, phone)
                Result.success(_currentUser.value)
            }
        } catch (e: Exception) {
            updateUserProfile(name, neighborhood, bio, _currentUser.value.skills, phone)
            Result.success(_currentUser.value)
        }
    }

    suspend fun uploadProfilePhoto(
        bytes: ByteArray,
        mimeType: String = "image/jpeg",
        fileName: String = "avatar.jpg"
    ): Result<User> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val mediaType = mimeType.toMediaTypeOrNull()
            val requestBody = bytes.toRequestBody(mediaType)
            val part = MultipartBody.Part.createFormData("photo", fileName, requestBody)
            val response = apiService.uploadProfilePhoto(part)
            if (response.isSuccessful && response.body()?.success == true) {
                val user = mapUserDtoToDomain(response.body()!!.data!!)
                _currentUser.value = user
                Result.success(user)
            } else {
                val errorMsg = response.body()?.message ?: "Failed to upload profile photo (HTTP ${response.code()})"
                _errorMessage.value = errorMsg
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val msg = e.message ?: "Failed to upload profile photo"
            _errorMessage.value = msg
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun logout(): Boolean {
        var serverRevoked = false
        try {
            val response = apiService.logout()
            if (response.isSuccessful && response.body()?.success == true) {
                serverRevoked = true
            }
        } catch (_: Exception) {
            // Server unavailable; local session will still be purged safely
        } finally {
            clearLocalUserSession()
        }
        return serverRevoked
    }

    fun clearLocalUserSession() {
        sessionManager.clearSession()
        _currentUser.value = User(
            id = "",
            fullName = "",
            email = "",
            phone = "",
            neighborhood = "",
            bio = "",
            skills = emptyList(),
            points = 0,
            completedTasksCount = 0,
            rating = 5.0f,
            isVerified = false
        )
    }

    private fun mapUserDtoToDomain(dto: UserDto): User {
        return User(
            id = dto.id,
            fullName = dto.fullName,
            email = dto.email,
            phone = dto.phone ?: "",
            neighborhood = dto.neighborhood,
            bio = dto.bio,
            avatarUrl = dto.avatarUrl,
            skills = dto.skills,
            isVerified = dto.isVerified,
            points = dto.points,
            completedTasksCount = dto.completedTasksCount,
            rating = dto.rating
        )
    }
}
