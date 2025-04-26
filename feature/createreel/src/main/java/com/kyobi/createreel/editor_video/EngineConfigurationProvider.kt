package com.kyobi.createreel.editor_video

import android.net.Uri
import androidx.compose.runtime.Composable
import com.kyobi.createreel.Secrets
import kotlinx.coroutines.launch
import ly.img.editor.EditorDefaults
import ly.img.editor.EngineConfiguration
import timber.log.Timber

@Composable
fun getEngineConfiguration(
    sceneUri: Uri,
    videoUri: Uri,
    userId: String?,
    editorVideoViewModel: EditorVideoViewModel
): EngineConfiguration {
    val tag = "EngineConfig"
    return EngineConfiguration.remember(
        license = Secrets.license,
        userId = userId,
        onCreate = {
            Timber.tag(tag).d("onCreate called with sceneUri: $sceneUri")
            val engine = this.editorContext.engine
            val eventHandler = this.editorContext.eventHandler
            EditorDefaults.onCreate(engine, sceneUri, eventHandler) { _, scope ->
                scope.launch {
                    try {
                        // Load scene trước
                        engine.scene.load(sceneUri)
                        Timber.tag(tag).d("Scene loaded successfully")
                        // Thêm video vào scene
                        engine.scene.createFromVideo(videoUri)
                        Timber.tag(tag).d("Video added to scene: $videoUri")

                        // Cấu hình Giphy asset source
                        editorVideoViewModel.configureEngine(engine, eventHandler, sceneUri)
                    } catch (e: Exception) {
                        Timber.tag(tag).e("Error loading scene or video: ${e.message}")
                    }
                }
            }
        },
        onError = { error ->
            Timber.tag(tag).d("onError called: $error")
        }
    )
}