package com.kyobi.trend.worker

import android.content.Context
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kyobi.core.di.NetworkModule
import com.kyobi.data.database.AppDatabase
import com.kyobi.data.storage.TokenStorageImpl
import com.kyobi.trend.cache.ReelMediaCache
import com.kyobi.trend.cache.ReelPreloadManager
import kotlin.system.measureTimeMillis
import timber.log.Timber

class ReelCleanupWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    @OptIn(UnstableApi::class)
    override suspend fun doWork(): Result {
        Timber.d("Running cleanup worker")
        return try {
            val timeTaken = measureTimeMillis {
                val database = AppDatabase.getDatabase(applicationContext)
                val mediaCache = ReelMediaCache(applicationContext)
                val tokenStorage = TokenStorageImpl(applicationContext)
                val okHttpClient = NetworkModule.provideKyobiOkHttpClient(tokenStorage)
                val preloadManager = ReelPreloadManager(
                    context = applicationContext,
                    mediaCache,
                    preloadedMediaDao = database.preloadedMediaDao(),
                    okHttpClient
                )
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