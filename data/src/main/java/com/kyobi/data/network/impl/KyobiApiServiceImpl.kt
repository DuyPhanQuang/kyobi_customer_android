package com.kyobi.data.network.impl

import com.kyobi.core.exceptions.ErrorHandler
import com.kyobi.core.exceptions.KyobiApiException
import com.kyobi.data.model.AnonymousLoginResponse
import com.kyobi.data.model.AppVersionResponse
import com.kyobi.data.model.AssetSourceResponse
import com.kyobi.data.model.AssetsResponse
import com.kyobi.data.model.AuthUserResponse
import com.kyobi.data.model.LoginResponse
import com.kyobi.data.model.NotificationResponse
import com.kyobi.data.model.SignupResponse
import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.request.LoginRequest
import com.kyobi.domain.model.request.RegisterTokenRequest
import com.kyobi.domain.model.request.SignupRequest
import com.kyobi.domain.model.request.UnregisterTokenRequest
import retrofit2.HttpException
import retrofit2.Retrofit
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KyobiApiServiceImpl @Inject constructor(
    retrofit: Retrofit,
    private val errorHandler: ErrorHandler
) : KyobiApiService {
    private val api = retrofit.create(KyobiApiService::class.java)
    private val tag = "KyobiApiService"

    override suspend fun login(request: LoginRequest): LoginResponse {
        try {
            Timber.tag(tag).d("Logging in with email: ${request.email}")
            return api.login(request)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            when (e.code()) {
                401 -> {
                    if (errorBody.contains("Email not confirmed")) {
                        throw KyobiApiException("Please verify your email to login", e.code())
                    } else {
                        throw KyobiApiException("Invalid email or password", e.code())
                    }
                }
                else -> throw errorHandler.handleError(e)
            }
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun loginAnonymously(): AnonymousLoginResponse {
        try {
            Timber.tag(tag).d("Logging in anonymously")
            return api.loginAnonymously()
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            when (e.code()) {
                500 -> {
                    if (errorBody.contains("Failed to create anonymous user")) {
                        throw KyobiApiException("Failed to create anonymous user", e.code())
                    } else {
                        throw KyobiApiException("Failed to sign in anonymously", e.code())
                    }
                }
                else -> throw errorHandler.handleError(e)
            }
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getAuthUser(): AuthUserResponse {
        try {
            Timber.tag(tag).d("Fetching auth user")
            return api.getAuthUser()
        } catch (e: HttpException) {
            if (e.code() == 401) {
                throw KyobiApiException("User not found", e.code())
            }
            throw errorHandler.handleError(e)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun logout() {
        try {
            Timber.tag(tag).d("Logging out")
            api.logout()
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun signup(request: SignupRequest): SignupResponse {
        try {
            Timber.tag(tag).d("Signing up with email: ${request.email}")
            return api.signup(request)
        } catch (e: HttpException) {
            val errorBody = e.response()?.errorBody()?.string() ?: ""
            when (e.code()) {
                400 -> {
                    if (errorBody.contains("User already registered")) {
                        return SignupResponse(
                            message = "Verification email resent",
                            data = null
                        )
                    } else if (errorBody.contains("is invalid")) {
                        throw KyobiApiException(
                            "The email address is invalid or cannot be used for signup. Please try a different email.",
                            e.code()
                        )
                    } else {
                        throw KyobiApiException(errorBody, e.code())
                    }
                }
                409 -> throw KyobiApiException("Email already registered and verified", e.code())
                else -> throw errorHandler.handleError(e)
            }
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getAppVersion(): AppVersionResponse {
        try {
            Timber.tag(tag).d("Fetching app version")
            return api.getAppVersion()
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun register(id: String, request: RegisterTokenRequest): NotificationResponse {
        try {
            Timber.tag(tag).d("Registering FCM token: ${request.token}")
            return api.register(id, request)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> throw KyobiApiException("Invalid or expired token", e.code())
                else -> throw errorHandler.handleError(e)
            }
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun unregister(id: String, request: UnregisterTokenRequest): NotificationResponse {
        try {
            Timber.tag(tag).d("Unregistering FCM token: ${request.token}")
            return api.unregister(id, request)
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> throw KyobiApiException("Invalid or expired token", e.code())
                else -> throw errorHandler.handleError(e)
            }
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getAssetSource(): AssetSourceResponse {
        try {
            Timber.tag(tag).d("Fetching asset source manifest")
            return api.getAssetSource()
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }

    override suspend fun getAssets(
        query: String?,
        page: Int?,
        perPage: Int?,
        locale: String?
    ): AssetsResponse {
        try {
            Timber.tag(tag).d("Fetching assets with query: $query, page: $page, perPage: $perPage")
            return api.getAssets(query, page, perPage, locale)
        } catch (e: Exception) {
            throw errorHandler.handleError(e)
        }
    }
}

