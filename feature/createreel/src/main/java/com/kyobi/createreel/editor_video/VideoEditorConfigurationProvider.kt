package com.kyobi.createreel.editor_video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.kyobi.createreel.editor_video.ui.EditorVideoOverlay
import com.kyobi.createreel.editor_video.ui.buildEditorVideoNavigationBarList
import com.kyobi.createreel.editor_video.ui.editorVideoDock
import ly.img.editor.EditorConfiguration
import ly.img.editor.EditorDefaults
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
    editorVideoViewModel: EditorVideoViewModel,
    isExporting: Boolean,
    animatedProgress: Float,
    onExportCancelled: () -> Unit,
): EditorConfiguration<EditorUiState> {
    val tag = "EditorConfigurationProvider"
    Timber.tag(tag).d("Creating EditorConfiguration")

    return EditorConfiguration.rememberForVideo(
        uiMode = EditorUiMode.DARK,
        assetLibrary = remember { editorVideoViewModel.getAssetLibrary() },
        onEvent = { state, event ->
            editorVideoViewModel.handleEditorEvent(editorContext.activity, state, event)
        },
        overlay = { state ->
            if (!isExporting) {
                EditorDefaults.Overlay(state = state, eventHandler = editorContext.eventHandler)
            } else EditorVideoOverlay(
                editorContext = editorContext,
                animatedProgress = animatedProgress,
                onExportCancelled = onExportCancelled
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