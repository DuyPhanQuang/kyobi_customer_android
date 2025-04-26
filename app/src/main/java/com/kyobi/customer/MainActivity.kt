package com.kyobi.customer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.kyobi.customer.ui.RequestNotificationPermissionIfNeeded
import com.kyobi.domain.usecase.AssetSourceUsecase
import com.kyobi.domain.usecase.AssetUsecase
import com.kyobi.featurecommon.auth.session.SessionEventBus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val tag = "MainActivity"
    @Inject
    lateinit var sessionEventBus: SessionEventBus

    @Inject
    lateinit var assetSourceUsecase: AssetSourceUsecase

    @Inject
    lateinit var assetUsecase: AssetUsecase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
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
                assetSourceUsecase = assetSourceUsecase,
                assetUsecase = assetUsecase
            )
        }
    }
}