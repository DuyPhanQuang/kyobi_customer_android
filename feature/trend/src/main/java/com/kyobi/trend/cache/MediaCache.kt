package com.kyobi.trend.cache

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class MediaCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val cache: SimpleCache by lazy {
        SimpleCache(
            File(context.cacheDir, "media_cache"),
            LeastRecentlyUsedCacheEvictor(50 * 1024 * 1024),
            StandaloneDatabaseProvider(context)
        )
    }

    fun obtainCache(): SimpleCache = cache

    fun release() {
        cache.release()
    }
}