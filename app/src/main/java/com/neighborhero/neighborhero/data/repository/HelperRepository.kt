package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.model.HelperProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HelperRepository {
    private val initialHelpers = listOf(
        HelperProfile(
            id = "helper_1",
            name = "Vikram Roy",
            title = "Neighborhood Hero",
            neighborhood = "Greenwood",
            distance = "0.4 miles away",
            rating = 4.9f,
            reviewCount = 128,
            tasksDone = 42,
            onTimePercentage = 99,
            bio = "Hey neighbor! I’m Vikram, a weekend DIY enthusiast and community handy-person. Always happy to lend a helping hand with heavy lifting, repairs, or garden cleanup.",
            skills = listOf("Heavy Lifting", "Furniture Assembly", "Yard Work", "Home Repairs", "Moving Help"),
            isVerified = true,
            isAvailableToday = true
        ),
        HelperProfile(
            id = "helper_2",
            name = "Aarav Sharma",
            title = "Senior Community Assistant",
            neighborhood = "Maplewood St.",
            distance = "0.7 miles away",
            rating = 5.0f,
            reviewCount = 94,
            tasksDone = 38,
            onTimePercentage = 100,
            bio = "Retired paramedic and garden volunteer. Happy to assist neighbors with grocery runs, medical drop-offs, and plant care.",
            skills = listOf("Groceries", "Pet Care", "Senior Support", "Plant Care"),
            isVerified = true,
            isAvailableToday = true
        ),
        HelperProfile(
            id = "helper_3",
            name = "Priya Patel",
            title = "Youth Tutor & Tech Guide",
            neighborhood = "Oakridge Park",
            distance = "1.1 miles away",
            rating = 4.9f,
            reviewCount = 67,
            tasksDone = 29,
            onTimePercentage = 98,
            bio = "Computer science graduate student offering tech troubleshooting, Wi-Fi setup, and STEM tutoring for middle schoolers.",
            skills = listOf("Tutoring", "Tech Support", "Wi-Fi Setup", "Coding Help"),
            isVerified = true,
            isAvailableToday = false
        )
    )

    private val _helpers = MutableStateFlow(initialHelpers)
    val helpers: StateFlow<List<HelperProfile>> = _helpers.asStateFlow()
}
