package com.neighborhero.neighborhero.viewmodel

import androidx.lifecycle.ViewModel
import com.neighborhero.neighborhero.data.model.HelpRequest
import com.neighborhero.neighborhero.data.model.RequestStatus
import com.neighborhero.neighborhero.data.repository.HelpRequestRepository
import kotlinx.coroutines.flow.StateFlow

class HelpRequestViewModel(
    private val repository: HelpRequestRepository = HelpRequestRepository()
) : ViewModel() {
    val requests: StateFlow<List<HelpRequest>> = repository.requests

    fun createRequest(
        title: String,
        category: String,
        description: String,
        time: String,
        location: String
    ) {
        val newReq = HelpRequest(
            id = "req_${System.currentTimeMillis()}",
            title = title,
            category = category,
            description = description,
            requesterName = "Eleanor Vance",
            requesterRating = 4.9f,
            requesterTaskCount = 24,
            location = location.ifBlank { "Maplewood District" },
            distance = "0.1 mi away",
            requestedTime = time.ifBlank { "Today, 4:00 PM" },
            isUrgent = false,
            status = RequestStatus.OPEN
        )
        repository.addRequest(newReq)
    }

    fun acceptRequest(requestId: String, helperName: String) {
        repository.updateRequestStatus(requestId, RequestStatus.IN_PROGRESS, helperName)
    }
}
