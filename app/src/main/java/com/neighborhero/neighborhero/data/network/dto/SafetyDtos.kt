package com.neighborhero.neighborhero.data.network.dto

import com.google.gson.annotations.SerializedName

data class TriggerSosDto(
    @SerializedName("latitude") val latitude: Double? = null,
    @SerializedName("longitude") val longitude: Double? = null,
    @SerializedName("address") val address: String = "Maplewood District",
    @SerializedName("alert_type") val alertType: String = "EMERGENCY_SOS"
)

data class SosAlertDto(
    @SerializedName("id") val id: String,
    @SerializedName("user_id") val userId: String,
    @SerializedName("alert_type") val alertType: String,
    @SerializedName("status") val status: String,
    @SerializedName("address") val address: String,
    @SerializedName("created_at") val createdAt: String
)

data class TaskHistoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("date") val date: String,
    @SerializedName("rating") val rating: Float,
    @SerializedName("status") val status: String,
    @SerializedName("category") val category: String
)

