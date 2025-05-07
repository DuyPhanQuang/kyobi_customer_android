package com.kyobi.core.di

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
import com.kyobi.core.exceptions.ShopifyErrorHandler
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApolloClientModule {
    @Provides
    @Singleton
    @Named("KyobiApolloClient")
    fun provideApolloClient(
        @Named("SHOPIFY_BASE_URL") shopifyBaseUrl: String,
        @Named("SHOPIFY_API_VERSION") shopifyApiVersion: String,
        @Named("X_SHOPIFY_STOREFRONT_ACCESS_TOKEN") xShopifyStorefrontAccessToken: String,
        @Named("ShopifyOkHttpClient") okHttpClient: OkHttpClient
    ): ApolloClient {
        val baseUrl = "${shopifyBaseUrl}${shopifyApiVersion}"
        return ApolloClient.Builder()
            .serverUrl(baseUrl)
            .addHttpHeader("X-Shopify-Storefront-Access-Token", xShopifyStorefrontAccessToken)
            .okHttpClient(okHttpClient)
            .build()
    }

    @Provides
    @Singleton
    fun provideShopifyErrorHandler(): ShopifyErrorHandler {
        return ShopifyErrorHandler()
    }
}