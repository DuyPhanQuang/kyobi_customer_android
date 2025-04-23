package com.kyobi.customer.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.kyobi.customer.global.version.AppVersionViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kyobi.customer.utils.AppIntentUtils
import com.kyobi.customer.utils.PackageNameType
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun RootUpdateVersionDialog() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val viewModel: AppVersionViewModel = hiltViewModel()
    val uiState = viewModel.uiState.collectAsState().value

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAppForeground()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                        onClick = { AppIntentUtils.openPlayStore(context, PackageNameType.Kyobi) }
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
                        onClick = { AppIntentUtils.openPlayStore(context, PackageNameType.Kyobi) }
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