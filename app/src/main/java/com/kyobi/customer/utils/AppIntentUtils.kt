package com.kyobi.customer.utils

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

enum class PackageNameType { Kyobi }

object AppIntentUtils {
    fun openPlayStore(context: Context, packageName: PackageNameType) {
        val id = when (packageName) {
            PackageNameType.Kyobi -> "com.kyobi.customer"
        }
        context.startActivity(
            Intent(Intent.ACTION_VIEW, "market://details?id=$id".toUri()).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
        )
    }
}