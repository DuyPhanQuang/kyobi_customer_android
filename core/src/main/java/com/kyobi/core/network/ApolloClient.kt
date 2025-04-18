package com.kyobi.core.network

import com.apollographql.apollo3.ApolloClient
import com.apollographql.apollo3.network.okHttpClient
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
    fun provideApolloClient(
        @Named("ShopifyOkHttpClient") okHttpClient: OkHttpClient
    ): ApolloClient {
        return ApolloClient.Builder()
            .serverUrl("https://xxx.myshopify.com/api/2025-01/graphql.json")
            .addHttpHeader("X-Shopify-Storefront-Access-Token", "xxx")
            .okHttpClient(okHttpClient)
            .build()
    }
}