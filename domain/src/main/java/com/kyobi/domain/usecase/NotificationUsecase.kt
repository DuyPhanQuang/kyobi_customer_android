package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Notification
import kotlinx.coroutines.flow.Flow

interface NotificationUseCase {
    suspend fun invokeRegister(userId: String, fcmToken: String): Flow<DomainNetworkResult<Notification>>
    suspend fun invokeUnregister(userId: String, fcmToken: String): Flow<DomainNetworkResult<Notification>>
}