package com.kyobi.data.di

import com.apollographql.apollo3.ApolloClient
import com.kyobi.core.exceptions.ShopifyErrorHandler
import com.kyobi.data.network.KyobiApiService
import com.kyobi.data.network.ShopifyApiService
import com.kyobi.data.network.impl.ShopifyApiServiceImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideKyobiApiService(
        @Named("KyobiRetrofitClient") retrofit: Retrofit
    ): KyobiApiService {
        return retrofit.create(KyobiApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideShopifyApiService(
        @Named("KyobiApolloClient") apolloClient: ApolloClient,
        errorHandler: ShopifyErrorHandler
    ): ShopifyApiService {
        return ShopifyApiServiceImpl(apolloClient, errorHandler)
    }
}