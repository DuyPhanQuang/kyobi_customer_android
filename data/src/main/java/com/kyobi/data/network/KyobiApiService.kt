package com.kyobi.data.network

import com.kyobi.core.model.RestNetworkResult
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
    suspend fun login(@Body request: LoginRequest): RestNetworkResult<LoginResponse>

    @POST("auth/anonymous-login")
    suspend fun loginAnonymously(): RestNetworkResult<AnonymousLoginResponse>

    @WithAuthHeaders
    @GET("auth/me")
    suspend fun getAuthUser(): RestNetworkResult<AuthUserResponse>

    @WithAuthHeaders
    @GET("auth/logout")
    suspend fun logout(): RestNetworkResult<Unit>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): RestNetworkResult<SignupResponse>

    @GET("app/status")
    suspend fun getAppVersion(): RestNetworkResult<AppVersionResponse>
}