package com.kyobi.core.di

import com.kyobi.core.network.NeedsAuthHeaders
import com.kyobi.core.network.WithAuthHeaders
import com.kyobi.core.storage.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Invocation
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    @Named("ShopifyOkHttpClient")
    fun provideShopifyOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @Named("KyobiOkHttpClient")
    fun provideKyobiOkHttpClient(tokenStorage: TokenStorage): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        // Interceptor để kiểm tra annotation và gán tag NeedsAuthHeaders
        val authHeaderInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            // Lấy invocation từ tag của request (Retrofit tự động gán invocation)
            val invocation = originalRequest.tag(Invocation::class.java)
            val needsAuthHeaders = invocation?.method()?.annotations?.any { it is WithAuthHeaders } ?: false
            // Thêm tag NeedsAuthHeaders vào request
            val taggedRequest = originalRequest.newBuilder()
                .tag(NeedsAuthHeaders::class.java, NeedsAuthHeaders(needsAuthHeaders))
                .build()

            chain.proceed(taggedRequest)
        }

        // Interceptor để thêm header dựa trên tag NeedsAuthHeaders
        val tokenInterceptor = Interceptor { chain ->
            val request = chain.request()
            val needsAuthHeadersTag = request.tag(NeedsAuthHeaders::class.java)
            val needsAuthHeaders = needsAuthHeadersTag?.value ?: false
            if (needsAuthHeaders) {
                val accessToken = tokenStorage.getAccessToken()
                val refreshToken = tokenStorage.getRefreshToken()
                val newRequest = request.newBuilder()
                if (accessToken != null) {
                    newRequest.addHeader("Authorization", "Bearer $accessToken")
                }
                if (refreshToken != null) {
                    newRequest.addHeader("X-Refresh-Token", refreshToken)
                }
                chain.proceed(newRequest.build())
            } else {
                chain.proceed(request)
            }
        }

        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor(authHeaderInterceptor) // Thêm Interceptor kiểm tra annotation
            .addInterceptor(tokenInterceptor) // Thêm Interceptor xử lý header
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}