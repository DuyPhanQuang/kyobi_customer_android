package com.kyobi.trend.cache

import android.content.Context
import androidx.media3.common.util.UnstableApi
import com.kyobi.core.extensions.toUniqueReelCacheKey
import com.kyobi.data.database.dao.PreloadedMediaDao
import com.kyobi.data.database.entity.PreloadedMediaEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import okhttp3.OkHttpClient
import okhttp3.Request
import timber.log.Timber
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentLinkedQueue
import javax.inject.Inject
import javax.inject.Singleton

const val TIMEOUT_VALUE = 5000L // in ms
const val MIN_FIRST_TS_CACHE_LENGTH_THRESHOLD = 500_000L

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
    private val saveMutex = Mutex()
    private val saveQueue = ConcurrentLinkedQueue<String>()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            while (scope.isActive) { // Chỉ chạy khi scope còn sống
                if (saveQueue.isEmpty()) {
                    yield() // Nhường CPU, không busy-wait
                    continue
                }
                val url = saveQueue.poll() ?: continue
                saveMutex.withLock {
                    try {
                        savePreloadedMedia(url)
                        Timber.tag(tag).d("Saved url=$url")
                    } catch (e: Exception) {
                        Timber.tag(tag).e(e, "Failed to save url=$url")
                    }
                }
            }
        }
    }

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


    suspend fun enqueueSavePreloadedMedia(url: String) = withContext(Dispatchers.IO) {
        if (!saveQueue.contains(url) && !preloadedUrls.containsKey(url)) {
            Timber.tag(tag).d("Enqueue url=$url")
            saveQueue.offer(url)
        }
    }

    private suspend fun savePreloadedMedia(url: String) = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val m3u8CacheKey = url.toUniqueReelCacheKey()
            val tsUrls = fetchTsUrls(url)
            val tsCacheKeys = tsUrls.map { it.toUniqueReelCacheKey() }
            val entity = PreloadedMediaEntity(
                url = url,
                cacheKey = m3u8CacheKey,
                tsCacheKeys = tsCacheKeys,
                timestamp = System.currentTimeMillis()
            )
            preloadedMediaDao.insert(entity)
            preloadedUrls[url] = m3u8CacheKey
            val firstTsCacheKey = tsCacheKeys.firstOrNull() ?: throw Exception("No tsCacheKeys for url=$url")
            var firstTsCachedLength = 0L
            val startPoll = System.currentTimeMillis()
            while (System.currentTimeMillis() - startPoll < TIMEOUT_VALUE) {
                val spans = mediaCache.getCache().getCachedSpans(firstTsCacheKey)
                if (spans.isNotEmpty() && spans.any { it.length > 0 }) {
                    firstTsCachedLength = mediaCache.getCache().getCachedLength(firstTsCacheKey, 0L, Long.MAX_VALUE)
                    if (firstTsCachedLength >= MIN_FIRST_TS_CACHE_LENGTH_THRESHOLD) {
                        Timber.tag(tag).d("Cached firstTsCacheKey=$firstTsCacheKey, length=$firstTsCachedLength")
                        break
                    }
                }
                yield() // Không delay, nhường cpu
            }
            if (firstTsCachedLength < MIN_FIRST_TS_CACHE_LENGTH_THRESHOLD) {
                Timber.tag(tag).w("Failed to cache firstTsCacheKey=$firstTsCacheKey, length=$firstTsCachedLength")
                throw Exception("Cache timeout for $firstTsCacheKey")
            }
            val durationMs = System.currentTimeMillis() - startTime
            Timber.tag(tag).d("Saved url=$url in ${durationMs}ms, firstTsCachedLength=$firstTsCachedLength")
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to save url=$url")
            throw e
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
        val entity = preloadedMediaDao.getByUrl(url) ?: return@withContext false
        val tsCacheKeys = entity.tsCacheKeys
        if (tsCacheKeys.isEmpty()) return@withContext false
        val firstTsCacheKey = tsCacheKeys.first()
        var firstTsCachedLength = 0L
        val startPoll = System.currentTimeMillis()
        while (System.currentTimeMillis() - startPoll < TIMEOUT_VALUE) {
            firstTsCachedLength = mediaCache.getCache().getCachedLength(firstTsCacheKey, 0L, Long.MAX_VALUE)
            if (firstTsCachedLength >= MIN_FIRST_TS_CACHE_LENGTH_THRESHOLD) break
            yield()
        }
        val isCached = firstTsCachedLength >= MIN_FIRST_TS_CACHE_LENGTH_THRESHOLD
        Timber.tag(tag).d("Checked url=$url, isCached=$isCached, firstTsCachedLength=$firstTsCachedLength")
        isCached
    }
}