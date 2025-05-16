package com.kyobi.trend.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.kyobi.data.database.dao.PreloadedMediaDao
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.cache.ReelPreloadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReelCacheModule {
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideReelPreloadManager(
        mediaCache: MediaCache,
        preloadedMediaDao: PreloadedMediaDao
    ): ReelPreloadManager {
        return ReelPreloadManager(mediaCache, preloadedMediaDao)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideMediaCache(
        @ApplicationContext context: Context
    ): MediaCache {
        return MediaCache(context)
    }
}