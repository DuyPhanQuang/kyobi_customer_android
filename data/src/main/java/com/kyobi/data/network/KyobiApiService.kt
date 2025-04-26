package com.kyobi.data.network

import com.kyobi.core.network.WithAuthHeaders
import com.kyobi.data.model.AuthUserResponse
import com.kyobi.data.model.AnonymousLoginResponse
import com.kyobi.data.model.AppVersionResponse
import com.kyobi.data.model.AssetSourceResponse
import com.kyobi.data.model.AssetsResponse
import com.kyobi.data.model.LoginResponse
import com.kyobi.data.model.NotificationResponse
import com.kyobi.data.model.SignupResponse
import com.kyobi.domain.model.request.LoginRequest
import com.kyobi.domain.model.request.RegisterTokenRequest
import com.kyobi.domain.model.request.SignupRequest
import com.kyobi.domain.model.request.UnregisterTokenRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

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

    @WithAuthHeaders
    @GET("app/status")
    suspend fun getAppVersion(): AppVersionResponse

    @WithAuthHeaders
    @POST("notification/{id}/register")
    suspend fun register(
        @Path("id") id: String,
        @Body request: RegisterTokenRequest
    ): NotificationResponse

    @WithAuthHeaders
    @POST("notification/{id}/unregister")
    suspend fun unregister(
        @Path("id") id: String,
        @Body request: UnregisterTokenRequest
    ): NotificationResponse

    @GET("api/assets/v1/giphy-stickers")
    suspend fun getAssetSource(): AssetSourceResponse

    @GET("api/assets/v1/giphy-stickers/assets")
    suspend fun getAssets(
        @Query("query") query: String? = "fashion style",
        @Query("page") page: Int? = 1,
        @Query("per_page") perPage: Int? = 10,
        @Query("locale") locale: String? = "en"
    ): AssetsResponse
}