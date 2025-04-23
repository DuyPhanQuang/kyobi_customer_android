package com.kyobi.data.network.impl

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.data.model.AnonymousLoginResponse
import com.kyobi.data.model.AppVersionResponse
import com.kyobi.data.model.AuthUserResponse
import com.kyobi.data.model.LoginResponse
import com.kyobi.data.model.SignupResponse
import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.request.LoginRequest
import com.kyobi.domain.model.request.SignupRequest
import retrofit2.HttpException
import retrofit2.Retrofit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class KyobiApiServiceImpl @Inject constructor(
    retrofit: Retrofit
) : KyobiApiService {
    private val api = retrofit.create(KyobiApiService::class.java)

    override suspend fun login(request: LoginRequest): RestNetworkResult<LoginResponse> {
        return try {
            val response = api.login(request)
            response
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> {
                    val errorMessage = e.response()?.errorBody()?.string() ?: "Unauthorized"
                    if (errorMessage.contains("Email not confirmed")) {
                        RestNetworkResult.Error("Please verify your email to login")
                    } else {
                        RestNetworkResult.Error("Invalid email or password")
                    }
                }
                429 -> RestNetworkResult.Error("Too many requests, please try again later")
                500 -> RestNetworkResult.Error("Server error, please try again later")
                else -> RestNetworkResult.Error("Error ${e.code()}: ${e.message()}")
            }
        } catch (e: Exception) {
            RestNetworkResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun loginAnonymously(): RestNetworkResult<AnonymousLoginResponse> {
        return try {
            val response = api.loginAnonymously()
            response
        } catch (e: HttpException) {
            when (e.code()) {
                429 -> RestNetworkResult.Error("Too many requests, please try again later")
                500 -> {
                    val errorMessage = e.response()?.errorBody()?.string() ?: "Server error"
                    if (errorMessage.contains("Failed to create anonymous user")) {
                        RestNetworkResult.Error("Failed to create anonymous user")
                    } else {
                        RestNetworkResult.Error("Failed to sign in anonymously")
                    }
                }
                else -> RestNetworkResult.Error("Error ${e.code()}: ${e.message()}")
            }
        } catch (e: Exception) {
            RestNetworkResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun getAuthUser(): RestNetworkResult<AuthUserResponse> {
        return try {
            val response = api.getAuthUser()
            response
        } catch (e: HttpException) {
            when (e.code()) {
                401 -> RestNetworkResult.Error("User not found")
                429 -> RestNetworkResult.Error("Too many requests, please try again later")
                500 -> RestNetworkResult.Error("Server error, please try again later")
                else -> RestNetworkResult.Error("Error ${e.code()}: ${e.message()}")
            }
        } catch (e: Exception) {
            RestNetworkResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun logout(): RestNetworkResult<Unit> {
        return try {
            api.logout()
            RestNetworkResult.Success(Unit)
        } catch (e: HttpException) {
            when (e.code()) {
                500 -> RestNetworkResult.Error("Server error, please try again later")
                else -> RestNetworkResult.Error("Error ${e.code()}: ${e.message()}")
            }
        } catch (e: Exception) {
            RestNetworkResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun signup(request: SignupRequest): RestNetworkResult<SignupResponse> {
        return try {
            val response = api.signup(request)
            response
        } catch (e: HttpException) {
            when (e.code()) {
                400 -> {
                    val errorMessage = e.response()?.errorBody()?.string() ?: "Invalid request"
                    if (errorMessage.contains("User already registered")) {
                        RestNetworkResult.Success(
                            SignupResponse(
                                message = "Verification email resent",
                                data = null
                            )
                        )
                    } else if (errorMessage.contains("is invalid")) {
                        RestNetworkResult.Error("The email address is invalid or cannot be used for signup. Please try a different email.")
                    } else {
                        RestNetworkResult.Error(errorMessage)
                    }
                }
                409 -> RestNetworkResult.Error("Email already registered and verified")
                429 -> RestNetworkResult.Error("Too many requests, please try again later")
                500 -> RestNetworkResult.Error("Server error, please try again later")
                else -> RestNetworkResult.Error("Error ${e.code()}: ${e.message()}")
            }
        } catch (e: Exception) {
            RestNetworkResult.Error("Network error: ${e.message}")
        }
    }

    override suspend fun getAppVersion(): RestNetworkResult<AppVersionResponse> {
        return try {
            val response = api.getAppVersion()
            response
        } catch (e: HttpException) {
            when (e.code()) {
                429 -> RestNetworkResult.Error("Too many requests, please try again later")
                500 -> RestNetworkResult.Error("Server error, please try again later")
                else -> RestNetworkResult.Error("Error ${e.code()}: ${e.message()}")
            }
        } catch (e: Exception) {
            RestNetworkResult.Error("Network error: ${e.message}")
        }
    }
}

