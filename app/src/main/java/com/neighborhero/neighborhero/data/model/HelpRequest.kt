package com.neighborhero.neighborhero.data.model

data class HelpRequest(
    val id: String,
    val title: String,
    val category: String,
    val description: String,
    val requesterName: String,
    val requesterRating: Float,
    val requesterTaskCount: Int,
    val location: String,
    val distance: String,
    val requestedTime: String,
    val isUrgent: Boolean = false,
    val status: RequestStatus = RequestStatus.OPEN,
    val assignedHelperId: String? = null,
    val assignedHelperName: String? = null,
    val requesterId: String? = null,
    val requesterPhone: String? = null
)

enum class RequestStatus {
    OPEN,
    IN_PROGRESS,
    PENDING_VERIFICATION,
    COMPLETED,
    CANCELLED
}
