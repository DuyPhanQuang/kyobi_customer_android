package com.kyobi.customer.global.worker

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.WorkManager
import com.kyobi.trend.worker.ReelCleanupWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerSetup @Inject constructor(
    private val workManager: WorkManager
) {
    fun scheduleCleanupWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
            .setRequiresBatteryNotLow(true)
            .build()
        val cleanupRequest = PeriodicWorkRequestBuilder<ReelCleanupWorker>(
            repeatInterval = 24, // Chạy mỗi 24h
            repeatIntervalTimeUnit = TimeUnit.HOURS,
            flexTimeInterval = 1,
            flexTimeIntervalUnit = TimeUnit.HOURS)
            .setConstraints(constraints)
            .setInitialDelay(1, TimeUnit.HOURS) // Trì hoãn 1h lần đầu
            .build()
        workManager.enqueueUniquePeriodicWork(
            "cleanup_preloaded_media",
            ExistingPeriodicWorkPolicy.KEEP,
            cleanupRequest
        )
    }
}