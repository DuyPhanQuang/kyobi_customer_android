package com.kyobi.trend.cache

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.kyobi.core.extensions.toUniqueReelCacheKey
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class MediaCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "MediaCache"
    private var cache: SimpleCache? = null
    private val cacheDir = File(context.cacheDir, "media_cache")
    private val cacheSizeMb = 500 // 500MB
    private var isCacheInUse = false

    init {
        clearCacheIfOld()
        cache = SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(cacheSizeMb * 1024 * 1024L),
            StandaloneDatabaseProvider(context)
        )
        Timber.tag(tag).d("Initialized cache at $cacheDir, contents: ${cacheDir.listFiles()?.joinToString()}")
    }

    fun getCache(): SimpleCache {
        if (cache == null) {
            cache = SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(cacheSizeMb * 1024 * 1024L),
                StandaloneDatabaseProvider(context)
            )
            Timber.tag(tag).d("Reinitialized cache with size ${cacheSizeMb}MB")
        }
        isCacheInUse = true
        return cache!!
    }

    private fun clearCache() {
        if (isCacheInUse) {
            Timber.tag(tag).d("Cache is in use, skipping clear")
            return
        }
        cache?.release()
        cache = null
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
            Timber.tag(tag).d("Cleared cache directory: ${cacheDir.path}")
        }
    }

    private fun clearCacheIfOld(maxAgeDays: Long = 1) {
        if (cacheDir.exists() && cacheDir.listFiles()?.isNotEmpty() == true) {
            val lastModified = cacheDir.lastModified()
            val currentTime = System.currentTimeMillis()
            val ageInDays = (currentTime - lastModified) / (1000 * 60 * 60 * 24)
            if (ageInDays > maxAgeDays) {
                clearCache()
                Timber.tag(tag).d("Cleared old cache (age: $ageInDays days)")
            }
        }
    }

    fun createSharedCacheDataSourceFactory(context: Context, mediaCache: SimpleCache): CacheDataSource.Factory {
        val upstreamFactory = DefaultDataSource.Factory(context)
        return CacheDataSource.Factory()
            .setCache(mediaCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheReadDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(CacheDataSink.Factory()
                .setCache(mediaCache))
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE or CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
            .setCacheKeyFactory { mediaItem ->
                val uriStr = mediaItem.uri.toString()
                uriStr.toUniqueReelCacheKey()
            }
    }

    fun createNonCachedDataSourceFactory(context: Context): DefaultDataSource.Factory {
        return DefaultDataSource.Factory(context)
    }

    fun getMediaSourceFactory(shouldCache: Boolean = true): DefaultMediaSourceFactory {
        val dataSourceFactory = if (shouldCache) {
            createSharedCacheDataSourceFactory(context, getCache())
        } else {
            createNonCachedDataSourceFactory(context)
        }
        return DefaultMediaSourceFactory(dataSourceFactory)
    }

    fun release() {
        cache?.release()
        cache = null
        isCacheInUse = false
        Timber.tag(tag).d("Released cache, contents: ${cacheDir.listFiles()?.joinToString()}")
    }
}