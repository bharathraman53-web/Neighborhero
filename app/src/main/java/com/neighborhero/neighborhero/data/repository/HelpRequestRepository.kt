package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.RequestStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HelpRequestRepository {
    private val initialRequests = listOf(
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

    private val _requests = MutableStateFlow(initialRequests)
    val requests: StateFlow<List<HelpRequest>> = _requests.asStateFlow()

    fun addRequest(request: HelpRequest) {
        _requests.value = listOf(request) + _requests.value
    }

    fun updateRequestStatus(requestId: String, newStatus: RequestStatus, helperName: String? = null) {
        _requests.value = _requests.value.map { req ->
            if (req.id == requestId) {
                req.copy(
                    status = newStatus,
                    assignedHelperName = helperName ?: req.assignedHelperName
                )
            } else req
        }
    }
}
