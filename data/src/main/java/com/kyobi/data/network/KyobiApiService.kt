package com.kyobi.data.network

import com.kyobi.data.model.AuthUserResponse
import com.kyobi.data.model.AnonymousLoginResponse
import com.kyobi.data.model.LoginResponse
import com.kyobi.data.model.request.LoginRequest
import retrofit2.Retrofit
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import javax.inject.Inject
import javax.inject.Singleton

interface KyobiApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/anonymous-login")
    suspend fun loginAnonymously(): AnonymousLoginResponse

    @GET("auth/me")
    suspend fun getAuthUser(): AuthUserResponse
}

@Singleton
class KyobiApiServiceImpl @Inject constructor(
    retrofit: Retrofit
) : KyobiApiService {
    private val api = retrofit.create(KyobiApiService::class.java)

    override suspend fun login(request: LoginRequest): LoginResponse {
        return try {
            api.login(request)
        } catch (e: Exception) {
            throw Exception("Failed to login: ${e.message}", e)
        }
    }

    override suspend fun loginAnonymously(): AnonymousLoginResponse {
        return try {
            api.loginAnonymously()
        } catch (e: Exception) {
            throw Exception("Failed to login anonymously: ${e.message}", e)
        }
    }

    override suspend fun getAuthUser(): AuthUserResponse {
        return try {
            api.getAuthUser()
        } catch (e: Exception) {
            throw Exception("Failed to get auth user: ${e.message}", e)
        }
    }
}

