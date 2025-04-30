package com.kyobi.trend.cache

import android.content.Context
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.multidex.BuildConfig
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
    private var cache: SimpleCache? = null
    private val cacheDir = File(context.cacheDir, "media_cache")
    private val cacheSizeMb = 100
    private var isCacheInUse = false // Thêm biến để theo dõi trạng thái sử dụng cache

    init {
        clearCacheIfOld()
        cache = SimpleCache(
            cacheDir,
            LeastRecentlyUsedCacheEvictor(cacheSizeMb * 1024 * 1024L),
            StandaloneDatabaseProvider(context)
        )
        Timber.tag("MediaCache").d("Initialized cache with size ${cacheSizeMb}MB")
    }

    fun getCache(): SimpleCache {
        if (cache == null) {
            cache = SimpleCache(
                cacheDir,
                LeastRecentlyUsedCacheEvictor(cacheSizeMb * 1024 * 1024L),
                StandaloneDatabaseProvider(context)
            )
            Timber.tag("MediaCache").d("Reinitialized cache with size ${cacheSizeMb}MB")
        }
        isCacheInUse = true // Đánh dấu cache đang được sử dụng
        return cache!!
    }

    private fun clearCache() {
        if (isCacheInUse) {
            Timber.tag("MediaCache").d("Cache is in use, skipping clear")
            return
        }
        cache?.release()
        cache = null
        if (cacheDir.exists()) {
            cacheDir.deleteRecursively()
            Timber.tag("MediaCache").d("Cleared cache directory: ${cacheDir.path}")
        }
    }

    private fun clearCacheIfOld(maxAgeDays: Long = 1) {
        if (cacheDir.exists() && cacheDir.listFiles()?.isNotEmpty() == true) {
            val lastModified = cacheDir.lastModified()
            val currentTime = System.currentTimeMillis()
            val ageInDays = (currentTime - lastModified) / (1000 * 60 * 60 * 24)
            if (ageInDays > maxAgeDays) {
                clearCache()
                Timber.tag("MediaCache").d("Cleared old cache (age: $ageInDays days)")
            }
        }
    }

    fun release() {
        cache?.release()
        cache = null
        isCacheInUse = false // Đặt lại trạng thái khi release
        if (BuildConfig.DEBUG) {
            if (cacheDir.exists()) {
                cacheDir.deleteRecursively()
                Timber.tag("MediaCache").d("Cleared cache directory: ${cacheDir.path}")
            }
        }
        Timber.tag("MediaCache").d("Released cache")
    }
}