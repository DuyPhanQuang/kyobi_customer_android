package com.kyobi.featurecommon.monitor.di

import android.content.Context
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import com.kyobi.featurecommon.monitor.network.NetworkUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MonitorModule {
    @Provides
    @Singleton
    fun provideNetworkUtils(@ApplicationContext context: Context): NetworkUtils {
        return NetworkUtils(context)
    }

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext context: Context,
        networkUtils: NetworkUtils
    ): NetworkMonitor {
        return NetworkMonitor(context, networkUtils)
    }
}