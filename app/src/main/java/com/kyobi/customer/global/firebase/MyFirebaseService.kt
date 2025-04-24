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
class MyFirebaseService @Inject constructor(
    private val notificationUsecase: NotificationUseCase
) {
    private val tag = "FCM"

    suspend fun refreshAndUploadToken(userId: String) {
        try {
            // Xóa token cũ
            FirebaseMessaging.getInstance().deleteToken().await()
            Timber.tag(tag).d("Deleted old token successfully")

            // Lấy token mới
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            Timber.tag(tag).d("Got refreshed token $fcmToken")

            // Gửi token lên server
            registerTokenWithServer(userId, fcmToken)
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to refresh and upload token")
            throw e
        }
    }

    suspend fun removeCurrentToken(userId: String) {
        try {
            // Lấy token hiện tại
            val fcmToken = FirebaseMessaging.getInstance().token.await()
            Timber.tag(tag).d("Removing token $fcmToken")

            if (userId.isNotEmpty()) {
                // Xóa token khỏi server chỉ khi có userId
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
        // Lấy kết quả cuối từ Flow, bỏ qua trạng thái Loading
        val result = notificationUsecase.invokeRegister(userId, token).first { it !is DomainNetworkResult.Loading }
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
                // Không xảy ra vì đã lọc Loading
                Timber.tag(tag).e("Unexpected result while registering token")
                throw IllegalStateException("Unexpected result while registering token")
            }
        }
    }

    private suspend fun unregisterTokenFromServer(userId: String, token: String) {
        Timber.tag(tag).d("Unregistering token from server: $token")
        // Lấy kết quả cuối từ Flow, bỏ qua trạng thái Loading
        val result = notificationUsecase.invokeUnregister(userId, token).first { it !is DomainNetworkResult.Loading }
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
                // Không xảy ra vì đã lọc Loading
                Timber.tag(tag).e("Unexpected result while unregistering token")
                throw IllegalStateException("Unexpected result while unregistering token")
            }
        }
    }
}