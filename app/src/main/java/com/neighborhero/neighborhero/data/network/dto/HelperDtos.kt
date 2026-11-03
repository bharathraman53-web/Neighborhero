package com.neighborhero.neighborhero.data.network.dto

import com.google.gson.annotations.SerializedName

data class HelperProfileDto(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("title") val title: String = "Neighborhood Hero",
    @SerializedName("neighborhood") val neighborhood: String = "Greenwood",
    @SerializedName("distance") val distance: String = "0.4 miles away",
    @SerializedName("rating") val rating: Float = 5.0f,
    @SerializedName("reviewCount") val reviewCount: Int = 0,
    @SerializedName("tasksDone") val tasksDone: Int = 0,
    @SerializedName("onTimePercentage") val onTimePercentage: Int = 100,
    @SerializedName("bio") val bio: String = "",
    @SerializedName("skills") val skills: List<String> = emptyList(),
    @SerializedName("isVerified") val isVerified: Boolean = true,
    @SerializedName("isAvailableToday") val isAvailableToday: Boolean = true,
    @SerializedName("avatarUrl") val avatarUrl: String? = null,
    @SerializedName("avatar_url") val avatar_url: String? = null,
    @SerializedName("points") val points: Int = 0,
    @SerializedName("match_score") val matchScore: Float? = null,
    @SerializedName("match_percentage") val matchPercentage: Int? = null,
    @SerializedName("matching_reasons") val matchingReasons: List<String>? = null,
    @SerializedName("active_tasks") val activeTasks: Int? = null
) {
    val resolvedAvatarUrl: String
        get() = avatarUrl?.takeIf { it.isNotBlank() } ?: avatar_url?.takeIf { it.isNotBlank() } ?: ""
}

