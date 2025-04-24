package com.kyobi.domain.di

import com.kyobi.domain.usecase.AppVersionUsecase
import com.kyobi.domain.usecase.GetProductsUseCase
import com.kyobi.domain.usecase.LoginUseCase
import com.kyobi.domain.usecase.LogoutUseCase
import com.kyobi.domain.usecase.NotificationUseCase
import com.kyobi.domain.usecase.SignUpUseCase
import com.kyobi.domain.usecase.impl.AppVersionUsecaseImpl
import com.kyobi.domain.usecase.impl.GetProductsUseCaseImpl
import com.kyobi.domain.usecase.impl.LoginUsecaseImpl
import com.kyobi.domain.usecase.impl.LogoutUsecaseImpl
import com.kyobi.domain.usecase.impl.NotificationUseCaseImpl
import com.kyobi.domain.usecase.impl.SignupUsecaseImpl
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

    @Binds
    @Singleton
    abstract fun bindLogoutUseCase(impl: LogoutUsecaseImpl): LogoutUseCase

    @Binds
    @Singleton
    abstract fun bindSignupUseCase(impl: SignupUsecaseImpl): SignUpUseCase

    @Binds
    @Singleton
    abstract fun bindAppVersionUsecase(impl: AppVersionUsecaseImpl): AppVersionUsecase

    @Binds
    @Singleton
    abstract fun bindNotificationUseCase(impl: NotificationUseCaseImpl): NotificationUseCase
}