package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.local.dao.HelpRequestDao
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.network.NeighborHeroApiService
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.dto.AssignHelperDto
import com.neighborhero.neighborhero.data.network.dto.CreateHelpRequestDto
import com.neighborhero.neighborhero.data.network.dto.HelpRequestDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HelpRequestRepository(
    private val apiService: NeighborHeroApiService = NetworkClient.getService(),
    private val cacheDao: HelpRequestDao? = null,
    seedForTests: Boolean = false
) {
    private val testSeedRequests = listOf(
        HelpRequest(
            id = "req_1",
            title = "Weekly Groceries Pickup",
            category = "Groceries",
            description = "Recovering from knee surgery and unable to drive. Need someone to pick up pre-paid groceries from Maplewood Co-op and drop them on front porch.",
            requesterName = "Martha W.",
            requesterRating = 4.9f,
            requesterTaskCount = 42,
            location = "Maplewood District",
            distance = "0.3 mi away",
            requestedTime = "Due Today, 4:00 PM",
            isUrgent = true,
            status = RequestStatus.OPEN
        ),
        HelpRequest(
            id = "req_2",
            title = "Garden Fence Repair & Mulch",
            category = "Repairs",
            description = "Storm damaged the backyard wooden fence panel. Need a handy neighbor to assist with securing 2 fence posts and laying fresh mulch.",
            requesterName = "David K.",
            requesterRating = 4.8f,
            requesterTaskCount = 19,
            location = "Oakridge Avenue",
            distance = "0.6 mi away",
            requestedTime = "Tomorrow, 10:00 AM",
            isUrgent = false,
            status = RequestStatus.OPEN
        ),
        HelpRequest(
            id = "req_3",
            title = "Golden Retriever Afternoon Walk",
            category = "Pet Care",
            description = "Looking for a dog lover to take Buster for a 30-minute stroll around Central Park while I am in afternoon meetings.",
            requesterName = "Sarah Jenkins",
            requesterRating = 5.0f,
            requesterTaskCount = 31,
            location = "Pinecrest Ave",
            distance = "0.8 mi away",
            requestedTime = "Today, 2:30 PM",
            isUrgent = false,
            status = RequestStatus.IN_PROGRESS,
            assignedHelperId = "user_1",
            assignedHelperName = "Eleanor Vance"
        ),
        HelpRequest(
            id = "req_4",
            title = "Algebra Tutoring for 8th Grader",
            category = "Tutoring",
            description = "Need 1 hour of patient algebra homework help for my son Leo before his midterm test on Friday.",
            requesterName = "Marcus Vance",
            requesterRating = 4.7f,
            requesterTaskCount = 12,
            location = "Block 4 Greenwood",
            distance = "1.2 mi away",
            requestedTime = "Thursday, 5:00 PM",
            isUrgent = false,
            status = RequestStatus.OPEN
        )
    )

    private val _requests = MutableStateFlow(
        if (seedForTests) testSeedRequests else (cacheDao?.getAll() ?: emptyList())
    )
    val requests: StateFlow<List<HelpRequest>> = _requests.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun addRequest(request: HelpRequest) {
        _requests.value = listOf(request) + _requests.value.filter { it.id != request.id }
        cacheDao?.insertOrUpdate(request)
    }

    fun replaceOrAddRequest(tempId: String?, request: HelpRequest) {
        val current = _requests.value
        val filtered = if (!tempId.isNullOrBlank()) {
            current.filter { it.id != tempId && it.id != request.id }
        } else {
            current.filter { it.id != request.id }
        }
        _requests.value = listOf(request) + filtered
        if (!tempId.isNullOrBlank()) {
            cacheDao?.deleteById(tempId)
        }
        cacheDao?.insertOrUpdate(request)
    }

    fun getRequestById(requestId: String): HelpRequest? {
        return _requests.value.find { it.id == requestId } ?: cacheDao?.getById(requestId)
    }

    fun updateRequestStatus(requestId: String, newStatus: RequestStatus, helperName: String? = null) {
        _requests.value = _requests.value.map { req ->
            if (req.id == requestId) {
                val updated = req.copy(
                    status = newStatus,
                    assignedHelperName = helperName ?: req.assignedHelperName
                )
                cacheDao?.insertOrUpdate(updated)
                updated
            } else req
        }
    }

    suspend fun fetchRequests(category: String? = null, status: String? = null): Result<List<HelpRequest>> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val response = apiService.getRequests(
                category = if (category == "All") null else category,
                status = status
            )
            if (response.isSuccessful && response.body()?.success == true) {
                val dtos = response.body()!!.data ?: emptyList()
                val mapped = dtos.map { mapDtoToDomain(it) }
                _requests.value = mapped
                cacheDao?.insertAll(mapped)
                Result.success(mapped)
            } else {
                val msg = response.body()?.message ?: "Failed to fetch requests"
                _errorMessage.value = msg
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message
            // Fall back to cached entries if offline
            val cached = cacheDao?.getAll() ?: _requests.value
            if (cached.isNotEmpty()) {
                _requests.value = cached
            }
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun createRemoteRequest(
        title: String,
        category: String,
        description: String,
        time: String,
        location: String,
        tempId: String? = null
    ): Result<HelpRequest> {
        _isLoading.value = true
        _errorMessage.value = null
        return try {
            val dto = CreateHelpRequestDto(
                title = title,
                category = category,
                description = description,
                location = location,
                requestedTime = time.ifBlank { "Due Today, 4:00 PM" }
            )
            val response = apiService.createRequest(dto)
            if (response.isSuccessful && response.body()?.success == true) {
                val created = mapDtoToDomain(response.body()!!.data!!)
                replaceOrAddRequest(tempId, created)
                Result.success(created)
            } else {
                val msg = response.body()?.message ?: "Failed to create request"
                _errorMessage.value = msg
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            _errorMessage.value = e.message
            Result.failure(e)
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun assignRemoteHelper(requestId: String, helperId: String? = null): Result<HelpRequest> {
        return try {
            val response = apiService.assignHelper(requestId, AssignHelperDto(helperId))
            if (response.isSuccessful && response.body()?.success == true) {
                val updated = mapDtoToDomain(response.body()!!.data!!)
                addRequest(updated)
                Result.success(updated)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to assign helper"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapDtoToDomain(dto: HelpRequestDto): HelpRequest {
        val st = try {
            RequestStatus.valueOf(dto.status.uppercase())
        } catch (e: Exception) {
            RequestStatus.OPEN
        }
        return HelpRequest(
            id = dto.id,
            title = dto.title,
            category = dto.category,
            description = dto.description,
            requesterName = dto.requesterName,
            requesterRating = dto.requesterRating,
            requesterTaskCount = dto.requesterTaskCount,
            location = dto.location,
            distance = dto.distance,
            requestedTime = dto.requestedTime,
            isUrgent = dto.isUrgent,
            status = st,
            assignedHelperId = dto.assignedHelperId,
            assignedHelperName = dto.assignedHelperName,
            requesterId = dto.requesterId,
            requesterPhone = dto.requesterPhone
        )
    }
}
