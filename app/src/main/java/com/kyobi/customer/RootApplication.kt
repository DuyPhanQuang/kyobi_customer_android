package com.kyobi.customer

import android.app.Application
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

const val ENGINE_ID = "1"

@HiltAndroidApp
class RootApplication : Application() {

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
}