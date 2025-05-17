package com.kyobi.trend.cache

import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.kyobi.core.extensions.toUniqueReelCacheKey
import com.kyobi.data.database.dao.PreloadedMediaDao
import com.kyobi.data.database.entity.PreloadedMediaEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@UnstableApi
@Singleton
class ReelPreloadManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: ReelMediaCache,
    private val preloadedMediaDao: PreloadedMediaDao,
    private val okHttpClient: OkHttpClient
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
            val cacheKey = url.toUniqueReelCacheKey()
            val tsUrls = fetchTsUrls(url)
            val tsCacheKeys = tsUrls.map { it.toUniqueReelCacheKey() }
            val entity = PreloadedMediaEntity(
                url = url,
                cacheKey = cacheKey,
                tsCacheKeys = tsCacheKeys,
                timestamp = System.currentTimeMillis()
            )
            preloadedMediaDao.insert(entity)
            preloadedUrls[url] = cacheKey
            val cachedLength = mediaCache.getCache().getCachedLength(cacheKey, 0L, Long.MAX_VALUE)
            val tsCachedLengths = tsCacheKeys.map {
                mediaCache.getCache().getCachedLength(it, 0L, Long.MAX_VALUE)
            }
            Timber.tag(tag).d(
                "Saved preloaded media: url=$url, cacheKey=$cacheKey, cachedLength=$cachedLength bytes, " +
                        "tsCacheKeys=${tsCacheKeys.joinToString()}, tsCachedLengths=${tsCachedLengths.joinToString()} bytes"
            )
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to save preloaded media for url=$url")
        }
    }

    private suspend fun fetchTsUrls(m3u8Url: String): List<String> = withContext(Dispatchers.IO) {
        try {
            // Tạo file tạm để lưu .m3u8
            val tempDir = context.cacheDir
            val tempFile = File(tempDir, "temp_${m3u8Url.hashCode()}.m3u8")
            // Tải .m3u8 về file tạm
            val request = Request.Builder().url(m3u8Url).build()
            okHttpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Timber.tag(tag).e("Failed to fetch .m3u8: ${response.code}")
                    return@withContext emptyList()
                }
                val body = response.body ?: return@withContext emptyList()
                try {
                    body.byteStream().use { input ->
                        tempFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                } catch (e: IOException) {
                    Timber.tag(tag).e(e, "Failed to save .m3u8 to temp file")
                    return@withContext emptyList()
                }
            }
            // Đọc nội dung file .m3u8
            val content = tempFile.readText()
            // Parse lấy URL .ts
            val tsUrls = content.lines()
                .filter { it.contains(".ts") && !it.startsWith("#") }
                .map { it.trim() }
            tempFile.delete()
            Timber.tag(tag).d("Fetched tsUrls: $tsUrls")
            tsUrls
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to fetch .ts URLs for $m3u8Url")
            emptyList()
        }
    }

    suspend fun isPreloadedAndCached(url: String): Boolean = withContext(Dispatchers.IO) {
        val entity = preloadedMediaDao.getByUrl(url) ?: run {
            Timber.tag(tag).w("No entity found for url=$url")
            return@withContext false
        }
        val cacheKey = entity.cacheKey
        val tsCacheKeys = entity.tsCacheKeys
        if (tsCacheKeys.isEmpty()) {
            Timber.tag(tag).w("No tsCacheKeys found for url=$url")
            return@withContext false
        }
        val minCachedLength = 100_000L // 100KB cho .ts
        val isTsCached = tsCacheKeys.all { tsCacheKey ->
            val cachedLength = mediaCache.getCache().getCachedLength(tsCacheKey, 0L, Long.MAX_VALUE)
            cachedLength >= minCachedLength
        }
        Timber.tag(tag).d(
            "Checked cache for url=$url, cacheKey=$cacheKey, tsCacheKeys=${tsCacheKeys.joinToString()}, " +
                    "isTsCached=$isTsCached"
        )
        return@withContext isTsCached
    }
}