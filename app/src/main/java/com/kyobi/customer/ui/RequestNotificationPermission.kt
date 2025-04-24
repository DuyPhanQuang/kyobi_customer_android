package com.kyobi.customer.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import timber.log.Timber

@Composable
fun RequestNotificationPermissionIfNeeded(
    onPermissionResult: (Boolean) -> Unit = {}
) {
    val context = LocalContext.current
    var hasRequestedPermission by remember { mutableStateOf(false) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Timber.tag("RequestNotificationPermission").d(
            if (isGranted) "Permission to send notifications has been granted."
            else "Denied permission to send notifications."
        )
        onPermissionResult(isGranted)
    }

    LaunchedEffect(Unit) {
        if (hasRequestedPermission) return@LaunchedEffect // Tránh yêu cầu nhiều lần

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val permissionStatus = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            )
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                hasRequestedPermission = true
            } else {
                onPermissionResult(true) // Quyền đã được cấp trước đó
            }
        } else {
            onPermissionResult(true) // Dưới Android 13 không cần quyền này
        }
    }
}