package com.kyobi.customer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import com.kyobi.customer.global.notification.MyFirebaseMessagingService
import com.kyobi.customer.ui.RequestNotificationPermissionIfNeeded
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            // xin cấp quyền thông báo
            RequestNotificationPermissionIfNeeded(
                onPermissionResult = { isGranted ->
                    if (isGranted) {
                        // Khởi động MyFirebaseMessagingService
                        startFcmService()
                    }
                }
            )
            RootApp()
        }
    }

    private fun startFcmService() {
        val intent = Intent(this, MyFirebaseMessagingService::class.java)
        intent.action = "com.google.firebase.MESSAGING_EVENT"
        startService(intent)
        Timber.tag("MyApplication").d("Sent signal to start fcm service")
    }
}