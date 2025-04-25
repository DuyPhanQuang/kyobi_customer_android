package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.KyobiApiException
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Notification
import com.kyobi.domain.model.request.RegisterTokenRequest
import com.kyobi.domain.model.request.UnregisterTokenRequest
import com.kyobi.domain.repository.NotificationRepository
import com.kyobi.domain.usecase.NotificationUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class NotificationUseCaseImpl @Inject constructor(
    private val notificationRepository: NotificationRepository
) : NotificationUseCase {

    override suspend fun invokeRegister(userId: String, fcmToken: String): Flow<DomainNetworkResult<Notification>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            val request = RegisterTokenRequest(token = fcmToken)
            val result = notificationRepository.registerToken(userId, request)
            emit(DomainNetworkResult.Success(result))
        }.catch { e ->
            emit(DomainNetworkResult.Error.Generic(e))
        }
    }

    override suspend fun invokeUnregister(userId: String, fcmToken: String): Flow<DomainNetworkResult<Notification>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            val request = UnregisterTokenRequest(token = fcmToken)
            val result = notificationRepository.unregisterToken(userId, request)
            emit(DomainNetworkResult.Success(result))
        }.catch { e ->
            emit(DomainNetworkResult.Error.Generic(e))
        }
    }
}