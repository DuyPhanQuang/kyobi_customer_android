package com.kyobi.featurecommon.auth.di

import com.kyobi.featurecommon.auth.AuthStateProvider
import com.kyobi.featurecommon.auth.AuthStateProviderImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthStateModule {

    @Binds
    @Singleton
    abstract fun provideAuthStateProvider(impl: AuthStateProviderImpl): AuthStateProvider
}