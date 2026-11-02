package com.neighborhero.neighborhero.data.network.dto

import com.google.gson.annotations.SerializedName

data class TaskHistoryDto(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String,
    @SerializedName("date") val date: String,
    @SerializedName("rating") val rating: Float,
    @SerializedName("status") val status: String,
    @SerializedName("category") val category: String
)
