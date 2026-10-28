package com.neighborhero.neighborhero.data.repository

import com.neighborhero.neighborhero.data.local.AppDatabaseHelper
import com.neighborhero.neighborhero.data.network.NetworkClient

object RepositoryProvider {
    val userRepository: UserRepository by lazy {
        UserRepository(
            apiService = NetworkClient.getService(),
            seedForTests = false
        )
    }

    val helpRequestRepository: HelpRequestRepository by lazy {
        HelpRequestRepository(
            apiService = NetworkClient.getService(),
            cacheDao = AppDatabaseHelper.getInstance().helpRequestDao,
            seedForTests = false
        )
    }

    val helperRepository: HelperRepository by lazy {
        HelperRepository(
            apiService = NetworkClient.getService(),
            cacheDao = AppDatabaseHelper.getInstance().helperDao,
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
            cacheDao = AppDatabaseHelper.getInstance().notificationDao,
            seedForTests = false
        )
    }
}
