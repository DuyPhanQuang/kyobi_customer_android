package com.kyobi.data.repository

import com.kyobi.core.storage.TokenStorage
import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.Notification
import com.kyobi.domain.model.request.RegisterTokenRequest
import com.kyobi.domain.model.request.UnregisterTokenRequest
import com.kyobi.domain.repository.NotificationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val apiService: KyobiApiService,
    private val tokenStorage: TokenStorage
) : NotificationRepository {

    override suspend fun registerToken(userId: String, request: RegisterTokenRequest): Notification {
        val response = apiService.register(userId, request)
        tokenStorage.saveFcmToken(request.token)
        return response.toNotification()
    }

    override suspend fun unregisterToken(userId: String, request: UnregisterTokenRequest): Notification {
        val response = apiService.unregister(userId, request)
        tokenStorage.clearFcmToken()
        return response.toNotification()
    }
}