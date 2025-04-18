package com.kyobi.data.di

import com.kyobi.data.repository.AuthRepositoryImpl
import com.kyobi.data.repository.ProductRepositoryImpl
import com.kyobi.domain.repository.AuthRepository
import com.kyobi.domain.repository.ProductRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindProductRepository(impl: ProductRepositoryImpl): ProductRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}