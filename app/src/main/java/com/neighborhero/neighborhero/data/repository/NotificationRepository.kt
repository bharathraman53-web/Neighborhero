package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.model.NotificationItem
import com.neighborhero.neighborhero.data.model.NotificationType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationRepository {
    private val initialNotifications = listOf(
        NotificationItem(
            id = "notif_1",
            title = "Offer Accepted!",
            message = "Martha W. accepted your help offer for Weekly Groceries Pickup.",
            timeAgo = "10 mins ago",
            type = NotificationType.REQUEST_ACCEPTED
        ),
        NotificationItem(
            id = "notif_2",
            title = "New Message from Eleanor",
            message = "\"Hi there! Thank you so much for picking up my grocery list...\"",
            timeAgo = "30 mins ago",
            type = NotificationType.NEW_MESSAGE
        ),
        NotificationItem(
            id = "notif_3",
            title = "Safety Alert: Weather Warning",
            message = "Heavy rain expected in Greenwood area today. Check in on elderly neighbors.",
            timeAgo = "2 hours ago",
            type = NotificationType.SAFETY_ALERT
        )
    )

    private val _notifications = MutableStateFlow(initialNotifications)
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()
}
