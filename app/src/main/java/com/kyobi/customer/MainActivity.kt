package com.kyobi.customer

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.kyobi.customer.ui.RequestNotificationPermissionIfNeeded
import com.kyobi.featurecommon.auth.session.SessionEventBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    private var navController: NavHostController? = null
    private var deepLinkState = mutableStateOf<Uri?>(null)

    @Inject
    lateinit var sessionEventBus: SessionEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Timber.tag(tag).d("onCreate called with intent: $intent")

        deepLinkState.value = intent.data

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            this.navController = navController

            RootApp(
                navController = navController,
                deepLinkState = deepLinkState
            )

            // xin cấp quyền thông báo
            RequestNotificationPermissionIfNeeded(
                onPermissionResult = { isGranted ->
                    lifecycleScope.launch {
                        sessionEventBus.emitNotificationPermissionGranted(isGranted)
                        if (isGranted) {
                            Timber.tag(tag).d("Notification permission granted")
                        } else {
                            Timber.tag(tag).w("Notification permission denied")
                        }
                    }
                }
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Timber.tag(tag).d("onNewIntent called with intent: $intent")
        setIntent(intent)
        deepLinkState.value = intent.data
    }

    override fun onResume() {
        super.onResume()
        Timber.tag(tag).d("onResume called with intent: $intent")
        // Kiểm tra intent mới trong onResume (phòng trường hợp onNewIntent không được gọi)
        val uri = intent.data
        if (uri != null && deepLinkState.value != uri) {
            Timber.tag(tag).d("Found deep link in onResume: $uri")
            deepLinkState.value = uri
        }
    }
}