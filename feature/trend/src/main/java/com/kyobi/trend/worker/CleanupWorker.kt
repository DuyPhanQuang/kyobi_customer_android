package com.kyobi.trend.worker

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kyobi.data.database.AppDatabase
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.cache.ReelPreloadManager
import kotlin.system.measureTimeMillis
import timber.log.Timber

class CleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    @OptIn(UnstableApi::class)
    override suspend fun doWork(): Result {
        Timber.d("Running cleanup worker")
        return try {
            val timeTaken = measureTimeMillis {
                val database = AppDatabase.getDatabase(applicationContext)
                val mediaCache = MediaCache(applicationContext)
                val preloadManager = ReelPreloadManager(mediaCache, database.preloadedMediaDao())
                preloadManager.cleanupOldRecords(maxAgeDays = 1)
            }
            Timber.d("Cleanup worker completed in $timeTaken ms")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Cleanup worker failed")
            Result.retry()
        }
    }
}