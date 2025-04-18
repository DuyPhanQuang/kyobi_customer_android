package com.kyobi.domain.di

import com.kyobi.domain.usecase.GetProductsUseCase
import com.kyobi.domain.usecase.LoginUseCase
import com.kyobi.domain.usecase.impl.GetProductsUseCaseImpl
import com.kyobi.domain.usecase.impl.LoginUsecaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {
    @Binds
    @Singleton
    abstract fun bindGetProductsUseCase(impl: GetProductsUseCaseImpl): GetProductsUseCase

    @Binds
    @Singleton
    abstract fun bindLoginUseCase(impl: LoginUsecaseImpl): LoginUseCase
}