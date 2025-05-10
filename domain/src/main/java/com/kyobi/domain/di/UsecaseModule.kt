package com.kyobi.domain.di

import com.kyobi.domain.usecase.AppVersionUsecase
import com.kyobi.domain.usecase.AssetSourceUsecase
import com.kyobi.domain.usecase.AssetUsecase
import com.kyobi.domain.usecase.GetBannersUseCase
import com.kyobi.domain.usecase.GetProductsUseCase
import com.kyobi.domain.usecase.GetUserUsecase
import com.kyobi.domain.usecase.LoginUseCase
import com.kyobi.domain.usecase.LogoutUseCase
import com.kyobi.domain.usecase.NotificationUseCase
import com.kyobi.domain.usecase.SignUpUseCase
import com.kyobi.domain.usecase.impl.AppVersionUsecaseImpl
import com.kyobi.domain.usecase.impl.AssetSourceUsecaseImpl
import com.kyobi.domain.usecase.impl.AssetUsecaseImpl
import com.kyobi.domain.usecase.impl.GetBannersUseCaseImpl
import com.kyobi.domain.usecase.impl.GetProductsUseCaseImpl
import com.kyobi.domain.usecase.impl.GetUserUsecaseImpl
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
    abstract fun bindGetUserUseCase(impl: GetUserUsecaseImpl): GetUserUsecase

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

    @Binds
    @Singleton
    abstract fun bindAssetSourceUseCase(impl: AssetSourceUsecaseImpl): AssetSourceUsecase

    @Binds
    @Singleton
    abstract fun bindAssetUseCase(impl: AssetUsecaseImpl): AssetUsecase

    @Binds
    @Singleton
    abstract fun bindGetBannersUseCase(impl: GetBannersUseCaseImpl): GetBannersUseCase
}