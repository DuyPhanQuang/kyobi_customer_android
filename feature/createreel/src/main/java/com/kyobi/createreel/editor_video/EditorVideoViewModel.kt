package com.kyobi.createreel.editor_video

import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import android.app.Activity
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.createreel.CreateReelUtils.saveVideoToGallery
import com.kyobi.domain.usecase.AssetSourceUsecase
import com.kyobi.domain.usecase.AssetUsecase
import com.kyobi.feature.createreel.R
import com.kyobi.createreel.editor_video.asset.addGiphyAssetSources
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ly.img.editor.DismissCloseConfirmationDialogEvent
import ly.img.editor.EditorDefaults
import ly.img.editor.EditorUiState
import ly.img.editor.HideLoading
import ly.img.editor.OnSceneLoaded
import ly.img.editor.ShareFileEvent
import ly.img.editor.ShowLoading
import ly.img.editor.ShowVideoExportErrorEvent
import ly.img.editor.ShowVideoExportProgressEvent
import ly.img.editor.ShowVideoExportSuccessEvent
import ly.img.editor.core.event.EditorEvent
import ly.img.editor.core.event.EditorEventHandler
import ly.img.editor.core.library.AssetLibrary
import ly.img.editor.core.library.AssetType
import ly.img.editor.core.library.LibraryCategory
import ly.img.editor.core.library.LibraryContent
import ly.img.editor.core.library.addSection
import ly.img.editor.core.library.data.AssetSourceType
import ly.img.engine.Engine
import ly.img.engine.SceneMode
import timber.log.Timber

@HiltViewModel
class EditorVideoViewModel @Inject constructor(
    private val assetSourceUsecase: AssetSourceUsecase,
    private val assetUsecase: AssetUsecase
) : ViewModel() {
    private val tag = "EditorVideoViewModel"

    // Trạng thái cho ProgressBar
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    fun getAssetLibrary(): AssetLibrary {
        Timber.tag("AssetLibrary").d("getAssetLibrary called")
        val giphyStickersAssetSourceType = AssetSourceType("giphy-stickers")
        val giphySection = LibraryContent.Section(
            titleRes = R.string.giphy,
            sourceTypes = listOf(giphyStickersAssetSourceType),
            assetType = AssetType.Sticker,
        )
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

    fun configureEngine(engine: Engine) {
        viewModelScope.launch {
            val isVideoScene = engine.scene.getMode() == SceneMode.VIDEO
            Timber.tag("engineConfiguration").d("isVideoScene $isVideoScene")
            if (isVideoScene) {
                try {
                    engine.addGiphyAssetSources(assetSourceUsecase, assetUsecase)
                    Timber.tag(tag).d("Added Giphy asset source successfully")
                } catch (e: Exception) {
                    Timber.tag(tag).e("Error configuring Giphy: ${e.message}")
                }
            } else {
                Timber.tag("engineConfiguration").w("Not a video scene, skipping Giphy asset source registration")
            }
        }
    }

    fun handleEditorEvent(
        activity: Activity,
        state: EditorUiState,
        event: EditorEvent
    ): EditorUiState {
        return when (event) {
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
                        activity,
                        "You have unsaved changes. Are you sure you want to exit?",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                EditorDefaults.onEvent(activity, state, event)
            }
            is ShowVideoExportSuccessEvent -> {
                val videoFile = event.file
                val mimeType = event.mimeType
                val videoPath = saveVideoToGallery(activity, videoFile)
                if (videoPath != null) {
                    Timber.tag(tag).d("videoPath: $videoPath")
                }
                _isExporting.value = false
                _exportProgress.value = 0f
                state
            }
            is ShowVideoExportErrorEvent -> {
                Toast.makeText(activity, "Export failed. Please try again.", Toast.LENGTH_SHORT).show()
                EditorDefaults.onEvent(activity, state, event)
            }
            is ShowVideoExportProgressEvent -> {
                Timber.tag(tag).d("Export progress: ${event.progress}")
                _isExporting.value = true
                _exportProgress.value = event.progress
                EditorDefaults.onEvent(activity, state, event)
            }
            is ShareFileEvent -> state
            else -> EditorDefaults.onEvent(activity, state, event)
        }
    }
}