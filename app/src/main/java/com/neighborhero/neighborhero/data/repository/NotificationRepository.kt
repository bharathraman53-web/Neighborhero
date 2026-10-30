package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.local.dao.NotificationDao
import com.neighborhero.neighborhero.data.model.NotificationItem
import com.neighborhero.neighborhero.data.model.NotificationType
import com.neighborhero.neighborhero.data.network.NeighborHeroApiService
import com.neighborhero.neighborhero.data.network.NetworkClient
import com.neighborhero.neighborhero.data.network.dto.NotificationDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationRepository(
    private val apiService: NeighborHeroApiService = NetworkClient.getService(),
    private val cacheDao: NotificationDao? = null,
    seedForTests: Boolean = false
) {
    private val testSeedNotifications = listOf(
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

    private val _notifications = MutableStateFlow(
        if (seedForTests) testSeedNotifications else (cacheDao?.getAll() ?: emptyList())
    )
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    fun addNotification(item: NotificationItem) {
        _notifications.value = listOf(item) + _notifications.value.filter { it.id != item.id }
        cacheDao?.insertOrUpdate(item)
    }

    suspend fun fetchNotifications(): Result<List<NotificationItem>> {
        return try {
            val response = apiService.getNotifications()
            if (response.isSuccessful && response.body()?.success == true) {
                val dtos = response.body()!!.data ?: emptyList()
                val mapped = dtos.map { mapDtoToDomain(it) }
                _notifications.value = mapped
                cacheDao?.insertAll(mapped)
                Result.success(mapped)
            } else {
                Result.failure(Exception(response.body()?.message ?: "Failed to fetch notifications"))
            }
        } catch (e: Exception) {
            val cached = cacheDao?.getAll() ?: _notifications.value
            if (cached.isNotEmpty()) {
                _notifications.value = cached
            }
            Result.failure(e)
        }
    }

    suspend fun markAsRead(id: String) {
        _notifications.value = _notifications.value.map {
            if (it.id == id) it.copy(isRead = true) else it
        }
        cacheDao?.markAsRead(id)
        try {
            apiService.markNotificationAsRead(id)
        } catch (_: Exception) {}
    }

    suspend fun markAllAsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        cacheDao?.markAllAsRead()
        try {
            apiService.markAllNotificationsAsRead()
        } catch (_: Exception) {}
    }

    fun clear() {
        _notifications.value = emptyList()
        cacheDao?.clear()
    }

    private fun mapDtoToDomain(dto: NotificationDto): NotificationItem {
        val type = try {
            NotificationType.valueOf(dto.type.uppercase())
        } catch (e: Exception) {
            NotificationType.COMMUNITY_NEWS
        }
        return NotificationItem(
            id = dto.id,
            title = dto.title,
            message = dto.message,
            timeAgo = dto.timeAgo,
            type = type,
            isRead = dto.isRead,
            relatedRequestId = dto.relatedRequestId
        )
    }
}
