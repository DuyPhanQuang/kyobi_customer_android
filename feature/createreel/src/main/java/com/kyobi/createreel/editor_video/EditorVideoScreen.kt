package com.kyobi.createreel.editor_video

import android.app.Activity
import android.net.Uri
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.kyobi.theme.kyobiTheme
import ly.img.camera.core.CameraResult
import ly.img.editor.VideoEditor
import timber.log.Timber

@Composable
fun EditorVideoScreen(
    selectType: SelectMediaType,
    uri: Uri?,
    cameraResult: CameraResult.Record?,
    userId: String?,
    editorVideoViewModel: EditorVideoViewModel,
    isExporting: Boolean,
    animatedProgress: Float,
    onClose: () -> Unit
) {
    val tag = "EditorVideoScreen"
    val context = LocalContext.current

    val activity = context as? Activity
        ?: throw IllegalStateException("EditorVideoScreen must be used within an Activity")

    // Làm fullscreen bằng WindowInsetsController
    DisposableEffect(Unit) {
        // Android 11 trở lên
        activity.window?.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            activity.window?.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.kyobiTheme.colors.onBackground)
    ) {
        VideoEditor(
            engineConfiguration = getEngineConfiguration(selectType, uri, cameraResult, userId, editorVideoViewModel),
            editorConfiguration = getVideoEditorConfiguration(
                editorVideoViewModel,
                isExporting,
                animatedProgress,
                onExportCancelled = { editorVideoViewModel.resetExportProgress() }
            ),
        ) {
            Timber.tag(tag).d("VideoEditor closed, invoking onClose")
            onClose()
        }
    }
}