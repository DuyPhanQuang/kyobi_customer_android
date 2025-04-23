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
}