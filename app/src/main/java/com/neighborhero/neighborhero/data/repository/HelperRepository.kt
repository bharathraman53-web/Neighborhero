package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.local.dao.HelperDao
import com.neighborhero.neighborhero.data.model.HelperProfile
import com.neighborhero.neighborhero.data.network.NeighborHeroApiService
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.dto.HelperProfileDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HelperRepository(
    private val apiService: NeighborHeroApiService = NetworkClient.getService(),
    private val cacheDao: HelperDao? = null,
    seedForTests: Boolean = false
) {
    private val testSeedHelpers = listOf(
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

    private val _helpers = MutableStateFlow(
        if (seedForTests) testSeedHelpers else (cacheDao?.getAll() ?: emptyList())
    )
    val helpers: StateFlow<List<HelperProfile>> = _helpers.asStateFlow()

    private val _leaderboard = MutableStateFlow<List<HelperProfile>>(emptyList())
    val leaderboard: StateFlow<List<HelperProfile>> = _leaderboard.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getHelperById(helperId: String): HelperProfile? {
        return _helpers.value.find { it.id == helperId } ?: cacheDao?.getById(helperId)
    }

    suspend fun fetchHelpers(skill: String? = null): Result<List<HelperProfile>> {
        _isLoading.value = true
        return try {
            val response = apiService.getHelpers(skill = skill)
            if (response.isSuccessful && response.body()?.success == true) {
                val dtos = response.body()!!.data ?: emptyList()
                val mapped = dtos.map { mapDtoToDomain(it) }
                _helpers.value = mapped
                cacheDao?.insertAll(mapped)
                Result.success(mapped)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch helpers"))
            }
        } catch (e: Exception) {
            val cached = cacheDao?.getAll() ?: _helpers.value
            if (cached.isNotEmpty()) {
                _helpers.value = cached
            }
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun fetchLeaderboard(): Result<List<HelperProfile>> {
        return try {
            val response = apiService.getLeaderboard()
            if (response.isSuccessful && response.body()?.success == true) {
                val dtos = response.body()!!.data ?: emptyList()
                val mapped = dtos.map { mapDtoToDomain(it) }
                _leaderboard.value = mapped
                Result.success(mapped)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch leaderboard"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapDtoToDomain(dto: HelperProfileDto): HelperProfile {
        return HelperProfile(
            id = dto.id,
            name = dto.name,
            title = dto.title,
            neighborhood = dto.neighborhood,
            distance = dto.distance,
            rating = dto.rating,
            reviewCount = dto.reviewCount,
            tasksDone = dto.tasksDone,
            onTimePercentage = dto.onTimePercentage,
            bio = dto.bio,
            skills = dto.skills,
            isVerified = dto.isVerified,
            isAvailableToday = dto.isAvailableToday,
            avatarUrl = dto.avatarUrl
        )
    }
}
