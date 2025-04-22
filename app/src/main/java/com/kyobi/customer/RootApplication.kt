package com.kyobi.customer

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import javax.inject.Inject

const val ENGINE_ID = "1"

@HiltAndroidApp
class RootApplication : Application() {
    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate() {
        super.onCreate()

        val appInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
            )
        } else {
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
        }

        val debug = true
        if (debug) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        networkMonitor.cleanup()
        Timber.tag("MyApplication").d("Cleaned up NetworkMonitor")
    }
}