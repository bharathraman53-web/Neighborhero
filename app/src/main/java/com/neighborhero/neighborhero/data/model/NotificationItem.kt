package com.neighborhero.neighborhero.data.model

data class NotificationItem(
    val id: String,
    val title: String,
    val message: String,
    val timeAgo: String,
    val type: NotificationType,
    val isRead: Boolean = false
)

enum class NotificationType {
    REQUEST_ACCEPTED,
    NEW_MESSAGE,
    TASK_UPDATE,
    SAFETY_ALERT,
    COMMUNITY_NEWS
}
