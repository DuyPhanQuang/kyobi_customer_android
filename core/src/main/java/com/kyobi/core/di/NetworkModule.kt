package com.kyobi.core.di

import com.kyobi.core.network.WithAuthHeaders
import com.kyobi.core.storage.TokenStorage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
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
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request()
                val annotations = chain.request().tag(java.lang.reflect.Method::class.java)
                    ?.annotations ?: emptyArray()
                val needsAuthHeaders = annotations.any { it is WithAuthHeaders }
                if (needsAuthHeaders) {
                    val accessToken = runBlocking { tokenStorage.getAccessToken() }
                    val refreshToken = runBlocking { tokenStorage.getRefreshToken() }
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
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
}