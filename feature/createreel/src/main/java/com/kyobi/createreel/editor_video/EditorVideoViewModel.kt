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
import ly.img.editor.core.library.AssetLibrary
import ly.img.editor.core.library.AssetType
import ly.img.editor.core.library.LibraryCategory
import ly.img.editor.core.library.LibraryContent
import ly.img.editor.core.library.addSection
import ly.img.editor.core.library.data.AssetSourceType
import ly.img.engine.Engine
import ly.img.engine.SceneMode
import ly.img.engine.populateAssetSource
import timber.log.Timber
import androidx.core.net.toUri
import ly.img.editor.DismissVideoExportEvent
import ly.img.engine.AssetDefinition
import ly.img.engine.AssetPayload

@HiltViewModel
class EditorVideoViewModel @Inject constructor(
    private val assetSourceUsecase: AssetSourceUsecase,
    private val assetUsecase: AssetUsecase
) : ViewModel() {
    private val tag = "EditorVideoViewModel"
    private val stickerMiscId = "ly.img.sticker.misc"

    // Trạng thái cho ProgressBar
    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _exportProgress = MutableStateFlow(0f)
    val exportProgress: StateFlow<Float> = _exportProgress.asStateFlow()

    fun resetExportProgress() {
        _isExporting.value = false
        _exportProgress.value = 0f
    }

    fun getAssetLibrary(): AssetLibrary {
        Timber.tag(tag).d("getAssetLibrary called")
        val giphyStickersAssetSourceType = AssetSourceType("giphy-stickers")
        val giphySection = LibraryContent.Section(
            titleRes = R.string.giphy,
            sourceTypes = listOf(giphyStickersAssetSourceType),
            assetType = AssetType.Sticker,
        )
        val customStickers = LibraryCategory.Stickers.addSection(giphySection)
        Timber.tag(tag).d("Configuring AssetLibrary with Giphy section, sourceId: giphy-stickers")

        // Section cho Custom Audio
        val customAudioAssetSourceType = AssetSourceType("custom-audio")
        val audioSection = LibraryContent.Section(
            titleRes = R.string.audio,
            sourceTypes = listOf(customAudioAssetSourceType),
            assetType = AssetType.Audio,
        )
        val customAudios = LibraryCategory.Audio.addSection(audioSection)
        Timber.tag(tag).d("Configuring AssetLibrary with Custom Audio section, sourceId: custom-audio")

        return AssetLibrary.getDefault(
            tabs = listOf(
                AssetLibrary.Tab.VIDEOS,
                AssetLibrary.Tab.IMAGES,
                AssetLibrary.Tab.AUDIOS,
                AssetLibrary.Tab.TEXT,
                AssetLibrary.Tab.SHAPES,
                AssetLibrary.Tab.STICKERS,
            ),
            stickers = customStickers,
            audios = customAudios
        )
    }

    private fun configureAudioAssetSource(engine: Engine) {
        // Tạo source cho audio
        val audioSourceId = "custom-audio"
        engine.asset.addLocalSource(
            sourceId = audioSourceId,
            supportedMimeTypes = listOf("audio/mp3") // Hỗ trợ MP3, có thể thêm WAV nếu cần
        )

        // Thêm từng audio asset
        val audioAssets = listOf(
            AssetDefinition(
                id = "audio1",
                label = mapOf(
                    "en" to "Background Music 1",
                    "es" to "Música de fondo 1"
                ),
                tags = mapOf(
                    "en" to listOf("background", "music", "relaxing"),
                    "es" to listOf("fondo", "música", "relajante")
                ),
                meta = mapOf(
                    "uri" to "file:///android_asset/audio/background_music1.mp3",
                    "thumbUri" to "file:///android_asset/audio/background_music1_thumb.png", // Thumbnail nếu có
                    "mimeType" to "audio/mp3",
                    "duration" to "120" // Thời lượng (giây), tùy chọn
                ),
                payload = AssetPayload() // Không cần payload đặc biệt cho audio
            ),
            AssetDefinition(
                id = "audio2",
                label = mapOf(
                    "en" to "Background Music 2",
                    "es" to "Música de fondo 2"
                ),
                tags = mapOf(
                    "en" to listOf("background", "music", "upbeat"),
                    "es" to listOf("fondo", "música", "alegre")
                ),
                meta = mapOf(
                    "uri" to "file:///android_asset/audio/background_music2.mp3",
                    "thumbUri" to "file:///android_asset/audio/background_music2_thumb.png",
                    "mimeType" to "audio/mp3",
                    "duration" to "150"
                ),
                payload = AssetPayload()
            )
        )

        // Thêm từng asset vào source
        audioAssets.forEach { asset ->
            engine.asset.addAsset(
                sourceId = audioSourceId,
                asset = asset
            )
        }
    }

    fun configureEngine(engine: Engine) {
        viewModelScope.launch {
            val isVideoScene = engine.scene.getMode() == SceneMode.VIDEO
            Timber.tag(tag).d("isVideoScene $isVideoScene")
            if (isVideoScene) {
                try {
                    engine.addGiphyAssetSources(assetSourceUsecase, assetUsecase)
                    Timber.tag(tag).d("Added Giphy asset source successfully")

                    // Giữ nguyên phần populateAssetSource cho sticker mặc định
                    engine.populateAssetSource(
                        id = stickerMiscId,
                        jsonUri = "https://cdn.img.ly/assets/demo/v2/ly.img.sticker.misc/content.json".toUri(),
                        replaceBaseUri = "https://cdn.img.ly/assets/demo/v2/ly.img.sticker.misc".toUri(),
                    )

                    configureAudioAssetSource(engine)
                    Timber.tag(tag).d("Added Audio asset source successfully")
                } catch (e: Exception) {
                    Timber.tag(tag).e("Error configuring Giphy, Audio : ${e.message}")
                }
            } else {
                Timber.tag(tag).w("Not a video scene, skipping Giphy, Audio asset source registration")
            }
        }
    }

    fun handleEditorEvent(activity: Activity, state: EditorUiState, event: EditorEvent): EditorUiState {
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
            is DismissVideoExportEvent -> {
                val hasChanges = true
                if (hasChanges) {
                    Toast.makeText(activity, "Export Cancelled", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(activity, "Export video failed. Please try again.", Toast.LENGTH_SHORT).show()
                EditorDefaults.onEvent(activity, state, event)
            }
            is ShowVideoExportProgressEvent -> {
                Timber.tag(tag).d("Export video progress: ${event.progress}")
                _isExporting.value = true
                _exportProgress.value = event.progress
                EditorDefaults.onEvent(activity, state, event)
            }
            is ShareFileEvent -> state
            else -> EditorDefaults.onEvent(activity, state, event)
        }
    }
}