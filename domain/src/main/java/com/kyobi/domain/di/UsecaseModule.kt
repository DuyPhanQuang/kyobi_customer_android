package com.kyobi.domain.di

import com.kyobi.domain.usecase.AppVersionUseCase
import com.kyobi.domain.usecase.AssetSourceUseCase
import com.kyobi.domain.usecase.AssetUseCase
import com.kyobi.domain.usecase.GetFlashSaleUseCase
import com.kyobi.domain.usecase.GetHomePagesUseCase
import com.kyobi.domain.usecase.GetProductRecommendationsUseCase
import com.kyobi.domain.usecase.GetProductsByIdsUseCase
import com.kyobi.domain.usecase.GetProductsUseCase
import com.kyobi.domain.usecase.GetUserUseCase
import com.kyobi.domain.usecase.LoginUseCase
import com.kyobi.domain.usecase.LogoutUseCase
import com.kyobi.domain.usecase.NotificationUseCase
import com.kyobi.domain.usecase.SignUpUseCase
import com.kyobi.domain.usecase.impl.AppVersionUseCaseImpl
import com.kyobi.domain.usecase.impl.AssetSourceUseCaseImpl
import com.kyobi.domain.usecase.impl.AssetUseCaseImpl
import com.kyobi.domain.usecase.impl.GetFlashSaleUseCaseImpl
import com.kyobi.domain.usecase.impl.GetHomePagesUseCaseImpl
import com.kyobi.domain.usecase.impl.GetProductRecommendationsUseCaseImpl
import com.kyobi.domain.usecase.impl.GetProductsByIdsUseCaseImpl
import com.kyobi.domain.usecase.impl.GetProductsUseCaseImpl
import com.kyobi.domain.usecase.impl.GetUserUseCaseImpl
import com.kyobi.domain.usecase.impl.LoginUseCaseImpl
import com.kyobi.domain.usecase.impl.LogoutUseCaseImpl
import com.kyobi.domain.usecase.impl.NotificationUseCaseImpl
import com.kyobi.domain.usecase.impl.SignupUseCaseImpl
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
    abstract fun bindGetUserUseCase(impl: GetUserUseCaseImpl): GetUserUseCase

    @Binds
    @Singleton
    abstract fun bindLoginUseCase(impl: LoginUseCaseImpl): LoginUseCase

    @Binds
    @Singleton
    abstract fun bindLogoutUseCase(impl: LogoutUseCaseImpl): LogoutUseCase

    @Binds
    @Singleton
    abstract fun bindSignupUseCase(impl: SignupUseCaseImpl): SignUpUseCase

    @Binds
    @Singleton
    abstract fun bindAppVersionUsecase(impl: AppVersionUseCaseImpl): AppVersionUseCase

    @Binds
    @Singleton
    abstract fun bindNotificationUseCase(impl: NotificationUseCaseImpl): NotificationUseCase

    @Binds
    @Singleton
    abstract fun bindAssetSourceUseCase(impl: AssetSourceUseCaseImpl): AssetSourceUseCase

    @Binds
    @Singleton
    abstract fun bindAssetUseCase(impl: AssetUseCaseImpl): AssetUseCase

    @Binds
    @Singleton
    abstract fun bindGetBannersUseCase(impl: GetHomePagesUseCaseImpl): GetHomePagesUseCase

    @Binds
    @Singleton
    abstract fun bindGetProductsByIdsUseCase(impl: GetProductsByIdsUseCaseImpl): GetProductsByIdsUseCase

    @Binds
    @Singleton
    abstract fun bindGetProductRecommendationsUseCase(impl: GetProductRecommendationsUseCaseImpl): GetProductRecommendationsUseCase

    @Binds
    @Singleton
    abstract fun bindGetFlashSaleUsecase(impl: GetFlashSaleUseCaseImpl): GetFlashSaleUseCase
}