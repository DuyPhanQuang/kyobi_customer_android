package com.kyobi.customer.di

import com.kyobi.customer.global.firebase.FcmUsecaseService
import com.kyobi.customer.global.firebase.FcmUsecaseServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ServiceComponent

@Module
@InstallIn(ServiceComponent::class)
abstract class UsecaseServiceModule {
    @Binds
    abstract fun bindFcmUsecaseService(impl: FcmUsecaseServiceImpl): FcmUsecaseService
}