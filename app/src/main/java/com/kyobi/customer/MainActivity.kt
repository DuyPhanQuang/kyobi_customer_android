package com.kyobi.customer

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.kyobi.customer.ui.RequestNotificationPermissionIfNeeded
import com.kyobi.featurecommon.auth.session.SessionEventBus
import com.kyobi.featurecommon.routes.Screen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    private var navController: NavHostController? = null

    @Inject
    lateinit var sessionEventBus: SessionEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val navController = rememberNavController()
            this.navController = navController

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
            RootApp(
                navController = navController
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent) {
        val uri = intent.data
        Timber.d("Received deeplink: $uri")
        val navController = this.navController ?: run {
            Timber.e("NavController is null, cannot handle deeplink")
            return
        }
        when (uri?.path) {
            "/home" -> navController.navigate(Screen.Home.routeScheme)
            "/trend" -> navController.navigate(Screen.Trend.routeScheme)
            else -> {
                Timber.w("Unknown deeplink path: ${uri?.path}, falling back to Home")
                navController.navigate(Screen.Home.routeScheme)
            }
        }
    }
}