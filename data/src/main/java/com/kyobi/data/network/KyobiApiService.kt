package com.kyobi.data.network

import com.kyobi.core.network.WithAuthHeaders
import com.kyobi.data.model.AuthUserResponse
import com.kyobi.data.model.AnonymousLoginResponse
import com.kyobi.data.model.AppVersionResponse
import com.kyobi.data.model.LoginResponse
import com.kyobi.data.model.SignupResponse
import com.kyobi.domain.model.request.LoginRequest
import com.kyobi.domain.model.request.SignupRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface KyobiApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse

    @POST("auth/anonymous-login")
    suspend fun loginAnonymously(): AnonymousLoginResponse

    @WithAuthHeaders
    @GET("auth/me")
    suspend fun getAuthUser(): AuthUserResponse

    @WithAuthHeaders
    @GET("auth/logout")
    suspend fun logout()

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): SignupResponse

    @GET("app/status")
    suspend fun getAppVersion(): AppVersionResponse
}