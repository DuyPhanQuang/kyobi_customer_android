package com.kyobi.authentication.di

import com.kyobi.authentication.AuthViewModel
import com.kyobi.domain.provider.auth.AuthStateProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthenticationModule {
    @Binds
    @Singleton
    abstract fun bindAuthStateProvider(authViewModel: AuthViewModel): AuthStateProvider
}