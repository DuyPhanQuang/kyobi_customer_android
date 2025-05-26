package com.kyobi.trend.di

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import com.kyobi.data.database.dao.PreloadedMediaDao
import com.kyobi.trend.cache.ReelMediaCache
import com.kyobi.trend.cache.ReelPreloadManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ReelCacheModule {
    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    fun provideReelPreloadManager(
        @ApplicationContext context: Context,
        @Named("KyobiMediaCache") mediaCache: ReelMediaCache,
        preloadedMediaDao: PreloadedMediaDao,
        @Named("KyobiOkHttpClient") okHttpClient: OkHttpClient
    ): ReelPreloadManager {
        return ReelPreloadManager(
            context,
            mediaCache,
            preloadedMediaDao,
            okHttpClient)
    }

    @OptIn(UnstableApi::class)
    @Provides
    @Singleton
    @Named("KyobiMediaCache")
    fun provideMediaCache(
        @ApplicationContext context: Context
    ): ReelMediaCache {
        return ReelMediaCache(context)
    }
}