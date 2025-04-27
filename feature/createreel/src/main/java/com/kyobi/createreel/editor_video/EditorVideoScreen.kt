package com.kyobi.createreel.editor_video

import android.app.Activity
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
    activity: Activity,
    isExporting: Boolean,
    animatedProgress: Float,
    onClose: () -> Unit
) {
    val tag = "EditorVideoScreen"
    Timber.tag(tag).d("EditorConfiguration created, initializing VideoEditor")

    Box(
        modifier =
            Modifier.fillMaxSize(),
    ) {
        VideoEditor(
            engineConfiguration = getEngineConfiguration(selectType, uri, cameraResult, userId, editorVideoViewModel),
            editorConfiguration = getVideoEditorConfiguration(activity, editorVideoViewModel, isExporting, animatedProgress),
        ) {
            Timber.tag(tag).d("VideoEditor closed, invoking onClose")
            onClose()
        }
    }
}