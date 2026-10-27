package com.neighborhero.neighborhero.data.model

data class HelperProfile(
    val id: String,
    val name: String,
    val title: String = "Neighborhood Hero",
    val neighborhood: String,
    val distance: String,
    val rating: Float,
    val reviewCount: Int,
    val tasksDone: Int,
    val onTimePercentage: Int,
    val bio: String,
    val skills: List<String>,
    val isVerified: Boolean = true,
    val isAvailableToday: Boolean = true,
    val avatarUrl: String = ""
)
