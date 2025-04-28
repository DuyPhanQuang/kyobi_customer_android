package com.kyobi.customer

import android.app.Application
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