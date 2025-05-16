package com.kyobi.customer

import android.app.Application
import android.os.StrictMode
import com.google.firebase.analytics.FirebaseAnalytics
import com.kyobi.customer.global.crashlytics.CrashReporter
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import com.kyobi.trend.worker.WorkManagerSetup
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import ly.img.engine.Engine

const val ENGINE_ID = "1"

@HiltAndroidApp
class RootApplication : Application() {
    private val tag = "MyApplication"

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    @Inject
    lateinit var firebaseAnalytics: FirebaseAnalytics

    @Inject
    lateinit var workManagerSetup: WorkManagerSetup

    override fun onCreate() {
        super.onCreate()

        val debug = BuildConfig.DEBUG
        if (debug) {
            Timber.plant(Timber.DebugTree())
        }

        // Bật StrictMode để phát hiện tác vụ nặng trên main thread
        if (!debug) {
            StrictMode.setThreadPolicy(
                StrictMode.ThreadPolicy.Builder()
                    .detectAll() // Phát hiện tất cả vấn đề (I/O, network, v.v.)
                    .penaltyLog() // Ghi log vào Logcat
                    .build()
            )
            StrictMode.setVmPolicy(
                StrictMode.VmPolicy.Builder()
                    .detectAll() // Phát hiện tất cả vi phạm (leak, v.v.)
                    .penaltyLog() // Ghi log vào Logcat
                    .build()
            )
        }

        // Tắt auto session tracking runtime
        firebaseAnalytics.setAnalyticsCollectionEnabled(false)
        // Global Crash Handler
        CoroutineScope(Dispatchers.IO).launch {
            CrashReporter.initGlobalHandler()
        }
        Timber.tag(tag).d("Initiated global crash handler")

        // Init Imgly Engine after 5s
        CoroutineScope(Dispatchers.Default).launch {
            delay(5000)
            Engine.init(this@RootApplication)
            Timber.tag(tag).d("Initialized Imgly Engine")
        }

        // Schedule cleanup
        workManagerSetup.scheduleCleanupWork()
    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor.cleanup()
        Timber.tag(tag).d("Cleaned up NetworkMonitor")
    }
}