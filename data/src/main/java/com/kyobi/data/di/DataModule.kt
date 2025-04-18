package com.kyobi.data.di

import com.kyobi.data.network.KyobiApiService
import com.kyobi.data.network.ShopifyApiService
import com.kyobi.data.network.ShopifyApiServiceImpl
import com.kyobi.data.network.impl.KyobiApiServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    abstract fun bindShopifyApiService(impl: ShopifyApiServiceImpl): ShopifyApiService

    @Binds
    abstract  fun bindKyobiApiService(impl: KyobiApiServiceImpl): KyobiApiService
}