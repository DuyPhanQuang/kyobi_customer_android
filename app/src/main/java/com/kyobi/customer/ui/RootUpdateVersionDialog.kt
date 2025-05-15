package com.kyobi.customer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.kyobi.customer.global.version.AppVersionViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.kyobi.core.utils.IntentUtils.openPlayStore
import com.kyobi.core.utils.PackageNameType
import com.kyobi.featurecommon.auth.session.Session
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber

@Composable
fun RootUpdateVersionDialog() {
    val tag = "RootUpdateVersionDialog"
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: AppVersionViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsState().value
    val hasCalledOnAppForeground = remember { mutableStateOf(false) }  // kiểm soát việc gọi onAppForeground

    /** Kịch bản call onAppForeground
     * Mở app lần sau, chưa đăng nhập → Không gọi onAppForeground().
     * Mở app lần sau, đã đăng nhập trước đó → Gọi onAppForeground().
     * Đăng nhập trong khi app đang chạy → Gọi onAppForeground().
     * Đăng xuất và đăng nhập lại → Gọi lại onAppForeground().
     * */
    // Theo dõi sessionEvents từ sessionEventBus
    val sessionState by produceState<Session?>(initialValue = null, lifecycleOwner) {
        viewModel.sessionEventBus.sessionEvents
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.RESUMED)
            .collectLatest { session ->
                value = session
                if (session == null) {
                    // Reset hasCalledOnAppForeground khi session là null (đăng xuất)
                    hasCalledOnAppForeground.value = false
                    Timber.tag(tag).d("Session is null, resetting hasCalledOnAppForeground")
                } else if (!hasCalledOnAppForeground.value) {
                    // Kiểm tra điều kiện khi sessionEvents emit giá trị không null
                    Timber.tag(tag).d("Conditions met: session exists (session=$session), calling onAppForeground")
                    viewModel.onAppForeground()
                    hasCalledOnAppForeground.value = true
                } else {
                    Timber.tag(tag).d("Conditions not met: session=$session, hasCalledOnAppForeground=${hasCalledOnAppForeground.value}, skipping onAppForeground")
                }
            }
    }


    when {
        // Hiển thị dialog maintenance
        uiState.isMaintenance -> {
            AlertDialog(
                onDismissRequest = { /* Non-dismissable */ },
                title = {
                    Text(
                        text = "Server Maintenance"
                    )},
                text = {
                    Text(
                        text = uiState.maintenanceMessage ?: "Server is under maintenance. Please try again later.")},
                confirmButton = {},
                dismissButton = {}
            )
        }
        // Hiển thị dialog force update
        uiState.showForceUpdate -> {
            AlertDialog(
                onDismissRequest = { /* Non-dismissable */ },
                title = { Text("Update Required") },
                text = { Text(uiState.forceUpdateMessage ?: "Please update to the latest version.") },
                confirmButton = {
                    Button(
                        onClick = { context.openPlayStore(PackageNameType.Kyobi) }
                    ) {
                        Text("Update Now")
                    }
                },
                dismissButton = {}
            )
        }
        // Update Notification Popup (Case 2.2)
        uiState.showUpdateNotification -> {
            AlertDialog(
                onDismissRequest = { viewModel.onUpdateNotificationDismissed() },
                title = {
                    Text(
                        text = "New Version Available"
                    )},
                text = {
                    Text(
                        text = uiState.updateNotificationMessage ?: "A new version is available. Update now for the best experience."
                    )},
                confirmButton = {
                    Button(
                        onClick = { context.openPlayStore(PackageNameType.Kyobi) }
                    ) {
                        Text("Update Now")
                    }
                },
                dismissButton = {
                    Button(
                        onClick = { viewModel.onUpdateNotificationDismissed() }
                    ) {
                        Text("Skip")
                    }
                }
            )
        }
    }
}