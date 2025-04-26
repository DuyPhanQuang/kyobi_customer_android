package com.kyobi.createreel

import android.app.Activity
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kyobi.createreel.VideoUtils.saveVideoToGallery
import com.kyobi.createreel.editor_video.EditorVideoOverlay
import com.kyobi.createreel.editor_video.editorVideoDock
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.theme.kyobiTheme
import kotlinx.coroutines.launch
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
import ly.img.engine.SceneMode
import timber.log.Timber
import com.kyobi.composable.icon.Homeoutline
import com.kyobi.composable.icon.IconPack
import com.kyobi.createreel.asset.addGiphyAssetSources
import com.kyobi.domain.usecase.AssetSourceUsecase
import com.kyobi.domain.usecase.AssetUsecase
import com.kyobi.feature.createreel.R
import ly.img.editor.core.component.EditorComponent.ListBuilder
import ly.img.editor.core.component.EditorComponent.ListBuilder.Companion.modify
import ly.img.editor.core.component.NavigationBar
import ly.img.editor.core.component.closeEditor
import ly.img.editor.core.component.rememberCloseEditor
import ly.img.editor.core.component.rememberForVideo
import ly.img.editor.core.library.AssetType
import ly.img.editor.core.library.LibraryCategory
import ly.img.editor.core.library.LibraryContent
import ly.img.editor.core.library.addSection
import ly.img.editor.core.library.data.AssetSourceType

@Composable
fun rememberEngineConfigurationForScene(
    sceneUri: Uri,
    assetSourceUsecase: AssetSourceUsecase,
    assetUsecase: AssetUsecase
): EngineConfiguration = EngineConfiguration.remember(
    license = Secrets.license,
    onCreate = {
        Timber.tag("EngineConfig").d("onCreate called with sceneUri: $sceneUri")
        val engine = this.editorContext.engine
        val eventHandler = this.editorContext.eventHandler
        EditorDefaults.onCreate(engine, sceneUri, eventHandler) { _, scope ->
            val isVideoScene = engine.scene.getMode() == SceneMode.VIDEO
            Timber.tag("engineConfiguration").d("isVideoScene $isVideoScene")
            if (isVideoScene) {
                scope.launch {
                    Timber.tag("engineConfiguration").d("Registering Giphy Asset Sources")
                    engine.addGiphyAssetSources(assetSourceUsecase, assetUsecase)
                }
            } else {
                Timber.tag("engineConfiguration").w("Not a video scene, skipping Giphy asset source registration")
            }
        }
    },
)

@OptIn(UnstableEditorApi::class)
@Composable
fun CreateReelTab(
    navController: NavController,
    sceneUri: Uri,
    authViewModel: AuthViewModel = hiltViewModel(),
    assetSourceUsecase: AssetSourceUsecase,
    assetUsecase: AssetUsecase,
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

    // Color Palette của Kyobi
    val kyobiColorPalette = listOf(
        MaterialTheme.kyobiTheme.colors.bg.logo,
        MaterialTheme.kyobiTheme.colors.primary,
        MaterialTheme.kyobiTheme.colors.secondary,
    )

    Timber.tag(tag).d("Creating EditorConfiguration")
    // Editor Configuration với các tùy chỉnh
    val editorConfiguration = EditorConfiguration.rememberForVideo(
        uiMode = EditorUiMode.DARK,
        assetLibrary = remember { getAssetLibrary() },
        colorPalette = remember { kyobiColorPalette },
        onEvent = { state, event ->
            when (event) {
                is OnSceneLoaded -> {
                    Timber.tag(tag).d("Scene loaded successfully")
                    EditorDefaults.onEvent(activity, state, event)
                }
                is ShowLoading -> {
                    Timber.tag(tag).d("Showing loading indicator")
                    state.copy(showLoading = true)
                }
                is HideLoading -> {
                    Timber.tag(tag).d("Hiding loading indicator")
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
                        Timber.tag(tag).d("videoPath: $videoPath")
                    }

                    // Ẩn ProgressBar
                    isExporting = false
                    exportProgress = 0f

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
        navigationBar = {
            NavigationBar.rememberForVideo(
                listBuilder = buildNavigationBarList(NavigationBar.ListBuilder.rememberForVideo()),
            )
        },
        canvasMenu = {
            CanvasMenu.remember()
        },
    )

    Timber.tag(tag).d("EditorConfiguration created, initializing VideoEditor")
    VideoEditor(
        engineConfiguration = rememberEngineConfigurationForScene(sceneUri, assetSourceUsecase, assetUsecase),
        editorConfiguration = editorConfiguration,
    ) {
        Timber.tag(tag).d("VideoEditor closed, popping back stack")
        // You can set result here
        navController.popBackStack()
    }
}

@Composable
private fun buildNavigationBarList(
    baseNavigationBar: ListBuilder<NavigationBar.Item<*>, Alignment.Horizontal, Arrangement.Horizontal>,
): ListBuilder<NavigationBar.Item<*>, Alignment.Horizontal, Arrangement.Horizontal> = baseNavigationBar.modify {
    replace(id = NavigationBar.Button.Id.closeEditor) {
        NavigationBar.Button.rememberCloseEditor(
            vectorIcon = { IconPack.Homeoutline },
        )
    }
}

private fun getAssetLibrary(): AssetLibrary {
    Timber.tag("AssetLibrary").d("getAssetLibrary called")
    val giphyStickersAssetSourceType = AssetSourceType("giphy-stickers")

    // Tạo section Giphy
    val giphySection = LibraryContent.Section(
        titleRes = R.string.giphy,
        sourceTypes = listOf(giphyStickersAssetSourceType),
        assetType = AssetType.Sticker,
        expandContent = LibraryContent.Stickers
    )

    // Tùy chỉnh LibraryCategory.Stickers
    val customStickers = LibraryCategory.Stickers.addSection(giphySection)

    Timber.tag("AssetLibrary").d("Configuring AssetLibrary with Giphy section, sourceId: giphy-stickers")

    return AssetLibrary.getDefault(
        tabs = listOf(
            AssetLibrary.Tab.VIDEOS,
            AssetLibrary.Tab.AUDIOS,
            AssetLibrary.Tab.TEXT,
            AssetLibrary.Tab.SHAPES,
            AssetLibrary.Tab.STICKERS,
        ),
        stickers = customStickers
    )
}