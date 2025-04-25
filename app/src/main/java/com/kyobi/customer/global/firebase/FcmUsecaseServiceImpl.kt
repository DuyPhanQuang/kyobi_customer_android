package com.kyobi.customer.global.firebase

import com.google.firebase.messaging.FirebaseMessaging
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.usecase.NotificationUseCase
import dagger.hilt.android.scopes.ServiceScoped
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import timber.log.Timber
import javax.inject.Inject

@ServiceScoped
class FcmUsecaseServiceImpl @Inject constructor(
    private val notificationUsecase: NotificationUseCase
): FcmUsecaseService {
    private val tag = "FcmUsecaseService"

    override suspend fun refreshAndUploadToken(userId: String) {
        try {
            FirebaseMessaging.getInstance().deleteToken().await()
            Timber.tag(tag).d("Deleted old token successfully")
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            Timber.tag(tag).d("Got refreshed token $fcmToken")
            registerTokenWithServer(userId, fcmToken)
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to refresh and upload token")
            throw e
        }
    }

    override suspend fun removeCurrentToken(userId: String) {
        try {
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            Timber.tag(tag).d("Removing token $fcmToken")

            if (userId.isNotEmpty()) {
                unregisterTokenFromServer(userId, fcmToken)
            } else {
                Timber.tag(tag).w("No userId provided, skipping unregister token from server")
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to remove token")
            throw e
        }
    }

    private suspend fun registerTokenWithServer(userId: String, token: String) {
        Timber.tag(tag).d("Registering token with server: $token")
        try {
            val result = notificationUsecase.invokeRegister(userId, token)
                .first { it !is DomainNetworkResult.Loading }
            when (result) {
                is DomainNetworkResult.Success -> {
                    Timber.tag(tag).d("Successfully registered token with server: ${result.data.message}")
                }
                is DomainNetworkResult.Error -> {
                    val errorMessage = when (result) {
                        is DomainNetworkResult.Error.KyobiApi -> result.exception.message
                        is DomainNetworkResult.Error.Generic -> result.throwable.message
                    } ?: "Something went wrong"
                    Timber.tag(tag).e("Failed to register token: $errorMessage")
                    throw Exception(errorMessage)
                }
                else -> {
                    Timber.tag(tag).e("Unexpected result while registering token: $result")
                    throw IllegalStateException("Unexpected result while registering token: $result")
                }
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error while registering token: ${e.message}")
            throw e
        }
    }

    private suspend fun unregisterTokenFromServer(userId: String, token: String) {
        Timber.tag(tag).d("Unregistering token from server: $token")
        try {
            val result = notificationUsecase.invokeUnregister(userId, token)
                .first { it !is DomainNetworkResult.Loading }
            when (result) {
                is DomainNetworkResult.Success -> {
                    Timber.tag(tag).d("Successfully unregistered token from server: ${result.data.message}")
                }
                is DomainNetworkResult.Error -> {
                    val errorMessage = when (result) {
                        is DomainNetworkResult.Error.KyobiApi -> result.exception.message
                        is DomainNetworkResult.Error.Generic -> result.throwable.message
                    } ?: "Something went wrong"
                    Timber.tag(tag).e("Failed to unregister token: $errorMessage")
                    throw Exception(errorMessage)
                }
                else -> {
                    Timber.tag(tag).e("Unexpected result while unregistering token: $result")
                    throw IllegalStateException("Unexpected result while unregistering token: $result")
                }
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error while unregistering token: ${e.message}")
            throw e
        }
    }
}