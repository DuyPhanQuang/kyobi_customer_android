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
class ReelMediaCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val tag = "ReelMediaCache"
    private val cacheDir = File(context.cacheDir, "reel_video_cache_folder")
    private val cacheSizeMb = 500 // 500MB
    private var isCacheInUse = false
    private val cacheSizeInBytes = cacheSizeMb * 1024 * 1024L

    companion object {
        @Volatile
        private var cache: SimpleCache? = null
        private val lock = Any()
    }

    init {
        // logic xóa file khóa khi khởi tạo ReelMediaCache để xử lý trường hợp app bị kill mà không gọi release()
        val lockFile = File(cacheDir, "exo_reel_video_cache_folder.lock")
        if (lockFile.exists()) {
            Timber.tag(tag).w("Lock file found, clearing cache directory")
            cacheDir.deleteRecursively()
        }
        clearCacheIfOld()
        synchronized(lock) {
            if (cache == null) {
                try {
                    cache = SimpleCache(
                        cacheDir,
                        LeastRecentlyUsedCacheEvictor(cacheSizeInBytes),
                        StandaloneDatabaseProvider(context)
                    )
                    Timber.tag(tag).d("Initialized cache at $cacheDir, contents: ${cacheDir.listFiles()?.joinToString()}")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to initialize cache, clearing directory and retrying")
                    cacheDir.deleteRecursively()
                    cache = SimpleCache(
                        cacheDir,
                        LeastRecentlyUsedCacheEvictor(cacheSizeInBytes),
                        StandaloneDatabaseProvider(context)
                    )
                }
            }
        }
    }

    fun getCache(): SimpleCache {
        synchronized(lock) {
            if (cache == null) {
                cache = SimpleCache(
                    cacheDir,
                    LeastRecentlyUsedCacheEvictor(cacheSizeInBytes),
                    StandaloneDatabaseProvider(context)
                )
                Timber.tag(tag).d("Reinitialized cache with size ${cacheSizeMb}MB")
            }
            isCacheInUse = true
            return cache!!
        }
    }

    private fun clearCache() {
        synchronized(lock) {
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
    }

    private fun clearCacheIfOld(maxAgeDays: Long = 1) {
        synchronized(lock) {
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
    }

    fun createSharedCacheDataSourceFactory(context: Context, mediaCache: SimpleCache): CacheDataSource.Factory {
        val upstreamFactory = DefaultDataSource.Factory(context)
        return CacheDataSource.Factory()
            .setCache(mediaCache)
            .setUpstreamDataSourceFactory(upstreamFactory)
            .setCacheReadDataSourceFactory(upstreamFactory)
            .setCacheWriteDataSinkFactory(CacheDataSink.Factory()
                .setCache(mediaCache))
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)
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
        synchronized(lock) {
            cache?.release()
            cache = null
            isCacheInUse = false
            Timber.tag(tag).d("Released cache, contents: ${cacheDir.listFiles()?.joinToString()}")
        }
    }
}