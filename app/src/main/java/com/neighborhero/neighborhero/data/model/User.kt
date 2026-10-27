package com.neighborhero.neighborhero.data.model

data class User(
    val id: String = "user_1",
    val fullName: String = "Eleanor Vance",
    val email: String = "eleanor@example.com",
    val phone: String = "(555) 234-5678",
    val neighborhood: String = "Maplewood & Oakridge",
    val bio: String = "Passionate gardener and retired teacher who loves helping out with local grocery runs, pet sitting, and weekend tutoring.",
    val avatarUrl: String = "",
    val skills: List<String> = listOf("Groceries", "Repairs", "Pet Care", "Tutoring"),
    val interests: List<String> = listOf("Urban Farming", "Book Clubs", "Senior Support"),
    val isVerified: Boolean = true,
    val points: Int = 350,
    val completedTasksCount: Int = 24,
    val rating: Float = 4.9f
)
