package com.kyobi.data.di

import com.kyobi.core.storage.TokenStorage
import com.kyobi.data.storage.TokenStorageImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {
    @Binds
    @Singleton
    abstract fun bindTokenStorage(impl: TokenStorageImpl): TokenStorage
}