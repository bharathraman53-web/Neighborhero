package com.neighborhero.neighborhero.data.repository

import android.content.Context
import com.neighborhero.neighborhero.data.local.AppDatabaseHelper
import com.neighborhero.neighborhero.data.network.NetworkClient

object RepositoryProvider {
    @Volatile
    private var initialized = false

    fun init(context: Context) {
        if (!initialized) {
            synchronized(this) {
                if (!initialized) {
                    AppDatabaseHelper.getInstance(context.applicationContext)
                    initialized = true
                }
            }
        }
    }

    fun isInitialized(): Boolean = initialized || AppDatabaseHelper.isInitialized()

    val userRepository: UserRepository by lazy {
        UserRepository(
            apiService = NetworkClient.getService(),
            seedForTests = false
        )
    }

    val helpRequestRepository: HelpRequestRepository by lazy {
        HelpRequestRepository(
            apiService = NetworkClient.getService(),
            cacheDao = if (isInitialized()) AppDatabaseHelper.getInstance().helpRequestDao else null,
            seedForTests = false
        )
    }

    val helperRepository: HelperRepository by lazy {
        HelperRepository(
            apiService = NetworkClient.getService(),
            cacheDao = if (isInitialized()) AppDatabaseHelper.getInstance().helperDao else null,
            seedForTests = false
        )
    }

    val chatRepository: ChatRepository by lazy {
        ChatRepository(
            apiService = NetworkClient.getService(),
            seedForTests = false
        )
    }

    val notificationRepository: NotificationRepository by lazy {
        NotificationRepository(
            apiService = NetworkClient.getService(),
            cacheDao = if (isInitialized()) AppDatabaseHelper.getInstance().notificationDao else null,
            seedForTests = false
        )
    }

    fun clearUserSession() {
        userRepository.clearLocalUserSession()
        notificationRepository.clear()
        chatRepository.clear()
    }
}
