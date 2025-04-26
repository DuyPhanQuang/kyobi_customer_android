package com.kyobi.createreel

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kyobi.createreel.VideoUtils.saveVideoToGallery
import com.kyobi.createreel.VideoUtils.shareVideo
import com.kyobi.createreel.editor_video.EditorVideoOverlay
import com.kyobi.createreel.editor_video.editorVideoDock
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.theme.kyobiTheme
import ly.img.editor.DismissCloseConfirmationDialogEvent
import ly.img.editor.EditorConfiguration
import ly.img.editor.EditorDefaults
import ly.img.editor.EditorUiMode
import ly.img.editor.EngineConfiguration
import ly.img.editor.HideLoading
import ly.img.editor.OnSceneLoaded
import ly.img.editor.ShareFileEvent
import ly.img.editor.ShowLoading
import ly.img.editor.ShowVideoExportErrorEvent
import ly.img.editor.ShowVideoExportProgressEvent
import ly.img.editor.ShowVideoExportSuccessEvent
import ly.img.editor.VideoEditor
import ly.img.editor.core.UnstableEditorApi
import ly.img.editor.core.component.CanvasMenu
import ly.img.editor.core.component.Dock
import ly.img.editor.core.component.InspectorBar
import ly.img.editor.core.library.AssetLibrary
import ly.img.editor.rememberForVideo
import timber.log.Timber

@OptIn(UnstableEditorApi::class)
@Composable
fun CreateReelTab(
    navController: NavController,
    authViewModel: AuthViewModel = hiltViewModel(),
) {
    val tag = "CreateReelTab"
    val authUiState = authViewModel.authUiState.collectAsStateWithLifecycle()
    val userId = authUiState.value.currentUser?.id
    val context = LocalContext.current
    // Ép kiểu context thành Activity một cách an toàn
    val activity = context as? Activity ?: throw IllegalStateException("Context must be an Activity")

    // Trạng thái cho ProgressBar
    var isExporting by remember { mutableStateOf(false) }
    var exportProgress by remember { mutableFloatStateOf(0f) }

    // Animation cho tiến độ
    val animatedProgress by animateFloatAsState(
        targetValue = exportProgress,
        animationSpec = tween(durationMillis = 200),
        label = "ExportProgressAnimation"
    )

    val engineConfiguration = EngineConfiguration.rememberForVideo(
        license = "08v2Jt1gMug5A3TRMnLd142Nn409IaCG_MY_1ZwQ_gDUZFtNrcKw4EHVojT5bjYK",
        userId = userId,
    )

    // Color Palette của Kyobi
    val kyobiColorPalette = listOf(
        MaterialTheme.kyobiTheme.colors.bg.logo,
        MaterialTheme.kyobiTheme.colors.primary,
        MaterialTheme.kyobiTheme.colors.secondary,
    )

    // Editor Configuration với các tùy chỉnh
    val editorConfiguration = EditorConfiguration.rememberForVideo(
        uiMode = EditorUiMode.DARK,
        assetLibrary = AssetLibrary.getDefault(),
        colorPalette = remember {
            kyobiColorPalette
        },
        onEvent = { state, event ->
            when (event) {
                is OnSceneLoaded -> {
                    Timber.tag(tag).d("User clicked Create Lookbook button")
                    EditorDefaults.onEvent(activity, state, event)
                }
                is ShowLoading -> {
                    state.copy(showLoading = true)
                }
                is HideLoading -> {
                    state.copy(showLoading = false)
                }
                is DismissCloseConfirmationDialogEvent -> {
                    val hasChanges = true
                    if (hasChanges) {
                        Toast.makeText(
                            context,
                            "You have unsaved changes. Are you sure you want to exit?",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    EditorDefaults.onEvent(activity, state, event)
                }
                is ShowVideoExportSuccessEvent -> {
                    // Lấy dữ liệu video đầu ra
                    val videoFile = event.file
                    val mimeType = event.mimeType

                    // Lưu vào gallery
                    val videoPath = saveVideoToGallery(context, videoFile)
                    if (videoPath != null) {
                        // Chia sẻ video
                        shareVideo(context, videoPath)

                        // Gọi API Create Reel
//                        createReel(context, videoFile)
                    }

                    // Ẩn ProgressBar
                    isExporting = false
                    exportProgress = 0f

//                    // Thoát editor
//                    navController.popBackStack()

                    // Trả về trạng thái hiện tại vì không cần thay đổi giao diện
                    state
                }
                is ShowVideoExportErrorEvent -> {
                    Toast.makeText(context, "Export failed. Please try again.", Toast.LENGTH_SHORT).show()
                    EditorDefaults.onEvent(activity, state, event)
                }
                is ShowVideoExportProgressEvent -> {
                    // Hiển thị tiến trình export (có thể thêm Toast hoặc UI tùy chỉnh)
                    Timber.tag(tag).d("Export progress: ${event.progress}")
                    isExporting = true
                    exportProgress = event.progress
                    EditorDefaults.onEvent(activity, state, event)
                }
                is ShareFileEvent -> {
                    state
                }
                else -> {
                    EditorDefaults.onEvent(activity, state, event)
                }
            }
        },
        overlay = { state ->
            EditorVideoOverlay(
                editorContext = editorContext,
                state = state,
                isExporting = isExporting,
                animatedProgress = animatedProgress
            )
        },
        dock = {
            Dock.remember(
                listBuilder = editorVideoDock()
            )
        },
        inspectorBar = {
            InspectorBar.remember()
        },
        canvasMenu = {
            CanvasMenu.remember()
        },
    )

    VideoEditor(
        engineConfiguration = engineConfiguration,
        editorConfiguration = editorConfiguration,
    ) {
        // You can set result here
        navController.popBackStack()
    }
}