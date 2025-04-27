package com.kyobi.createreel.editor_video

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kyobi.createreel.editor_video.ui.EditorVideoOverlay
import com.kyobi.createreel.editor_video.ui.buildEditorVideoNavigationBarList
import com.kyobi.createreel.editor_video.ui.editorVideoDock
import ly.img.editor.EditorConfiguration
import ly.img.editor.EditorUiMode
import ly.img.editor.EditorUiState
import ly.img.editor.core.UnstableEditorApi
import ly.img.editor.core.component.CanvasMenu
import ly.img.editor.core.component.Dock
import ly.img.editor.core.component.InspectorBar
import ly.img.editor.core.component.NavigationBar
import ly.img.editor.core.component.rememberForVideo
import ly.img.editor.rememberForVideo
import timber.log.Timber

@OptIn(UnstableEditorApi::class)
@Composable
fun getVideoEditorConfiguration(
    activity: Activity,
    editorVideoViewModel: EditorVideoViewModel,
    isExporting: Boolean,
    animatedProgress: Float
): EditorConfiguration<EditorUiState> {
    val tag = "EditorConfigurationProvider"
    Timber.tag(tag).d("Creating EditorConfiguration")

    return EditorConfiguration.rememberForVideo(
        uiMode = EditorUiMode.DARK,
        assetLibrary = remember { editorVideoViewModel.getAssetLibrary() },
        onEvent = { state, event ->
            editorVideoViewModel.handleEditorEvent(activity, state, event)
        },
        overlay = { state ->
            EditorVideoOverlay(
                editorContext = editorContext,
                state = state,
                isExporting = isExporting,
                animatedProgress = animatedProgress
            )
        },
        dock = { Dock.remember(listBuilder = editorVideoDock()) },
        inspectorBar = { InspectorBar.remember() },
        navigationBar = {
            NavigationBar.rememberForVideo(
                listBuilder = buildEditorVideoNavigationBarList(NavigationBar.ListBuilder.rememberForVideo()),
            )
        },
        canvasMenu = { CanvasMenu.remember() },
    )
}