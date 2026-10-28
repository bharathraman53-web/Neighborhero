package com.neighborhero.neighborhero.data.network.dto

import com.google.gson.annotations.SerializedName

data class NotificationDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("message") val message: String,
    @SerializedName("type") val type: String,
    @SerializedName("time_ago") val timeAgo: String,
    @SerializedName("is_read") val isRead: Boolean = false,
    @SerializedName("related_request_id") val relatedRequestId: String? = null
)

