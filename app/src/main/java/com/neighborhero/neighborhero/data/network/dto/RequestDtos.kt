package com.neighborhero.neighborhero.data.network.dto

import com.google.gson.annotations.SerializedName

data class CreateHelpRequestDto(
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String,
    @SerializedName("location") val location: String,
    @SerializedName("distance") val distance: String? = "0.4 mi away",
    @SerializedName("requested_time") val requestedTime: String? = null,
    @SerializedName("is_urgent") val isUrgent: Boolean = false
)

data class HelpRequestDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("description") val description: String,
    @SerializedName("requester_name") val requesterName: String,
    @SerializedName("requester_rating") val requesterRating: Float = 5.0f,
    @SerializedName("requester_task_count") val requesterTaskCount: Int = 0,
    @SerializedName("location") val location: String,
    @SerializedName("distance") val distance: String = "Nearby",
    @SerializedName("requested_time") val requestedTime: String,
    @SerializedName("is_urgent") val isUrgent: Boolean = false,
    @SerializedName("status") val status: String = "OPEN",
    @SerializedName("assigned_helper_id") val assignedHelperId: String? = null,
    @SerializedName("assigned_helper_name") val assignedHelperName: String? = null,
    @SerializedName("requester_id") val requesterId: String? = null,
    @SerializedName("requester_phone") val requesterPhone: String? = null,
    @SerializedName("created_at") val createdAt: String? = null
)

