package com.kyobi.customer.di

import com.kyobi.customer.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object AppBuildConfigModule {
    @Provides
    @Named("BASE_URL")
    fun provideBaseUrl(): String = BuildConfig.BASE_URL

    @Provides
    @Named("SHOPIFY_BASE_URL")
    fun provideShopifyBaseUrl(): String = BuildConfig.SHOPIFY_BASE_URL

    @Provides
    @Named("SHOPIFY_API_VERSION")
    fun provideShopifyApiVersion(): String = BuildConfig.SHOPIFY_API_VERSION

    @Provides
    @Named("X_SHOPIFY_STOREFRONT_ACCESS_TOKEN")
    fun provideXShopifyStorefrontAccessToken(): String = BuildConfig.X_SHOPIFY_STOREFRONT_ACCESS_TOKEN
}