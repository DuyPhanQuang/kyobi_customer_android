package com.kyobi.createreel.editor_video

import android.app.Activity
import android.net.Uri
import androidx.compose.runtime.Composable
import ly.img.editor.VideoEditor
import timber.log.Timber

@Composable
fun EditorVideoScreen(
    sceneUri: Uri,
    videoUri: Uri,
    userId: String?,
    editorVideoViewModel: EditorVideoViewModel,
    activity: Activity,
    isExporting: Boolean,
    animatedProgress: Float,
    onClose: () -> Unit
) {
    val tag = "EditorVideoScreen"
    Timber.tag(tag).d("EditorConfiguration created, initializing VideoEditor")
    VideoEditor(
        engineConfiguration = getEngineConfiguration(sceneUri, videoUri, userId, editorVideoViewModel),
        editorConfiguration = getVideoEditorConfiguration(activity, editorVideoViewModel, isExporting, animatedProgress),
    ) {
        Timber.tag(tag).d("VideoEditor closed, invoking onClose")
        onClose()
    }
}