package com.kyobi.domain.repository

import com.kyobi.domain.model.Notification
import com.kyobi.domain.model.request.RegisterTokenRequest
import com.kyobi.domain.model.request.UnregisterTokenRequest

interface NotificationRepository {
    suspend fun registerToken(userId: String, request: RegisterTokenRequest): Notification
    suspend fun unregisterToken(userId: String, request: UnregisterTokenRequest): Notification
}