package com.kyobi.createreel.editor_video

import android.media.MediaMetadataRetriever
import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import com.kyobi.createreel.Secrets
import kotlinx.coroutines.android.awaitFrame
import kotlinx.coroutines.launch
import ly.img.camera.core.CameraResult
import ly.img.editor.EditorDefaults
import ly.img.editor.EngineConfiguration
import ly.img.editor.core.UnstableEditorApi
import ly.img.editor.core.event.EditorEvent
import ly.img.editor.core.library.data.AssetSourceType
import timber.log.Timber

@OptIn(UnstableEditorApi::class)
@Composable
fun getEngineConfiguration(
    selectType: SelectMediaType,
    uri: Uri?,
    cameraResult: CameraResult.Record?,
    userId: String?,
    editorVideoViewModel: EditorVideoViewModel
): EngineConfiguration {
    val tag = "EngineConfig"
    val context = LocalContext.current
    var size: Size? = null

    when (selectType) {
        SelectMediaType.VIDEO -> {
            size =
                remember {
                    val retriever = MediaMetadataRetriever()
                    retriever.setDataSource(context, uri)
                    val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!.toInt()
                    val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!.toInt()
                    retriever.release()
                    Size(width.toFloat(), height.toFloat())
                }

            Timber.tag(tag).d("get size: $size")
        }
        SelectMediaType.CAMERA_RECORD -> {
            Timber.tag(tag).d("no need do anything $cameraResult")
        }
    }

    return EngineConfiguration.remember(
        license = Secrets.license,
        userId = userId,
        onCreate = {
            Timber.tag(tag).d("onCreate called with uri: $uri")
            val engine = editorContext.engine
            val eventHandler = editorContext.eventHandler
            EditorDefaults.onCreate(
                engine = engine,
                eventHandler = eventHandler,
                sceneUri = EngineConfiguration.defaultVideoSceneUri,
            ) { _, scope ->
                scope.launch {
                    when (selectType) {
                        SelectMediaType.VIDEO -> {
                            try {
                                val currentPage = engine.scene.getCurrentPage()!!
                                if (size != null) {
                                    engine.block.setWidth(currentPage, size.width)
                                    engine.block.setHeight(currentPage, size.height)
                                }
                                eventHandler.send(
                                    EditorEvent.AddUriToScene(
                                        uploadAssetSourceType = AssetSourceType.VideoUploads,
                                        uri = uri!!,
                                    ),
                                )
                                // Cấu hình Giphy asset source
                                editorVideoViewModel.configureEngine(engine)
                            } catch (e: Exception) {
                                Timber.tag(tag).e("Error add uri to scene: ${e.message}")
                            }
                            return@launch
                        }
                        SelectMediaType.CAMERA_RECORD -> {
                            try {
                                awaitFrame()
                                eventHandler.send(
                                    EditorEvent.AddCameraRecordingsToScene(
                                        uploadAssetSourceType = AssetSourceType.VideoUploads,
                                        recordings = cameraResult!!.recordings
                                            .flatMap { recording ->
                                                recording.videos
                                                    .map { it.uri to recording.duration }
                                            },
                                    ),
                                )
                            } catch (e: Exception) {
                                Timber.tag(tag).e("Error add camera recording to scene: ${e.message}")
                            }
                            return@launch
                        }
                    }
                }
            }
        },
        onError = { error ->
            Timber.tag(tag).d("onError called: $error")
        }
    )
}