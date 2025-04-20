package com.kyobi.trend.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.kyobi.trend.cache.MediaCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TrendModule {
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMediaCache(@ApplicationContext context: Context): MediaCache {
        return MediaCache(context)
    }
}