package com.neighborhero.neighborhero.data.network.dto

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("password") val password: String,
    @SerializedName("neighborhood") val neighborhood: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("skills") val skills: List<String>? = null
)

data class LoginRequest(
    @SerializedName("email_or_phone") val emailOrPhone: String,
    @SerializedName("password") val password: String
)

data class UpdateProfileRequest(
    @SerializedName("full_name") val fullName: String? = null,
    @SerializedName("neighborhood") val neighborhood: String? = null,
    @SerializedName("bio") val bio: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("skills") val skills: List<String>? = null
)

data class AuthResponseData(
    @SerializedName("token") val token: String,
    @SerializedName("user") val user: UserDto
)

data class UserDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("neighborhood") val neighborhood: String = "",
    @SerializedName("bio") val bio: String = "",
    @SerializedName("avatar_url") val avatarUrl: String = "",
    @SerializedName("is_verified") val isVerified: Boolean = true,
    @SerializedName("points") val points: Int = 0,
    @SerializedName("completed_tasks_count") val completedTasksCount: Int = 0,
    @SerializedName("rating") val rating: Float = 5.0f,
    @SerializedName("skills") val skills: List<String> = emptyList()
)

