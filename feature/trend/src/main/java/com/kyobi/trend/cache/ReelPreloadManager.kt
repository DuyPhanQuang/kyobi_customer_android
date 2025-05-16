package com.kyobi.trend.cache

import androidx.media3.common.util.UnstableApi
import com.kyobi.data.database.dao.PreloadedMediaDao
import com.kyobi.data.database.entity.PreloadedMediaEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class ReelPreloadManager @Inject constructor(
    private val mediaCache: MediaCache,
    private val preloadedMediaDao: PreloadedMediaDao
) {
    private val tag = "ReelPreloadManager"
    private val preloadedUrls = mutableMapOf<String, String>() // Cache { url: cacheKey }

    suspend fun cleanupOldRecords(maxAgeDays: Long = 1) = withContext(Dispatchers.IO) {
        val threshold = System.currentTimeMillis() - maxAgeDays * 24 * 60 * 60 * 1000
        preloadedMediaDao.deleteOlderThanWithLog(threshold)
        Timber.tag(tag).d("Cleaned up preloaded media older than $maxAgeDays days")
    }

    suspend fun loadPreloadedUrls() = withContext(Dispatchers.IO) {
        val entities = preloadedMediaDao.getAll()
        preloadedUrls.clear()
        entities.forEach { entity ->
            preloadedUrls[entity.url] = entity.cacheKey
        }
        Timber.tag(tag).d("Loaded ${entities.size} preloaded URLs from Room")
    }

    suspend fun savePreloadedMedia(url: String) = withContext(Dispatchers.IO) {
        try {
            val cacheKey = generateCacheKey(url)
            val entity = PreloadedMediaEntity(
                url = url,
                cacheKey = cacheKey,
                timestamp = System.currentTimeMillis()
            )
            preloadedMediaDao.insert(entity)
            preloadedUrls[url] = cacheKey
            val isCached = mediaCache.isCached(cacheKey, 0, Long.MAX_VALUE)
            Timber.tag(tag).d("Saved preloaded media: url=$url, cacheKey=$cacheKey, isCached=$isCached")
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to save preloaded media for url=$url")
        }
    }

    suspend fun isPreloadedAndCached(url: String): Boolean = withContext(Dispatchers.IO) {
        val cacheKey = preloadedUrls[url] ?: return@withContext false
        val isCached = mediaCache.isCached(cacheKey, 0L, Long.MAX_VALUE)
        Timber.tag(tag).d("Checked cache for url=$url, cacheKey=$cacheKey, isCached=$isCached")
        return@withContext isCached
    }

    fun generateCacheKey(url: String): String {
        val fileName = url.substringAfterLast("/").substringBefore("?")
        val hash = url.hashCode()
        val token = url.substringAfter("?token=").takeIf { it.isNotEmpty() } ?: "notoken"
        return "$fileName-$hash-${token.hashCode()}" // same key with MediaCache
    }
}