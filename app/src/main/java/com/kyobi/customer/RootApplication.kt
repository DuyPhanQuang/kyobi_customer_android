package com.kyobi.customer

import android.app.Application
import android.os.StrictMode
import com.kyobi.customer.global.crashlytics.CrashReporter
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject
import ly.img.engine.Engine

const val ENGINE_ID = "1"

@HiltAndroidApp
class RootApplication : Application() {
    private val tag = "MyApplication"

    @Inject
    lateinit var networkMonitor: NetworkMonitor

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

        // Global Crash Handler
        CrashReporter.initGlobalHandler()
        Timber.tag(tag).d("Initiated global crash handler")

        // Init Imgly Engine
        Engine.init(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor.cleanup()
        Timber.tag(tag).d("Cleaned up NetworkMonitor")
    }
}