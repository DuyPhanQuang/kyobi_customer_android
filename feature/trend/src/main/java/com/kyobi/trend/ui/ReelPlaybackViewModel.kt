package com.kyobi.trend.ui

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.Executors
import javax.inject.Inject
import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.DefaultHlsExtractorFactory

@HiltViewModel
class ReelPlaybackViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context,
    val mediaCache: MediaCache
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private val _reels = mutableStateOf<List<Reel>>(emptyList())
    val reels: State<List<Reel>> = _reels
    private val executorService = Executors.newFixedThreadPool(3)
    private val _isFetching = mutableStateOf(false)
    val isFetching: State<Boolean> = _isFetching

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        _reels.value = newReels
        preloadMediaSourceForRange(0, newReels.size)
        Timber.tag(tag).d("Preloaded initial ${newReels.size} media sources")
    }

    fun fetchMoreReels() {
        if (_isFetching.value) return
        _isFetching.value = true
        try {
            val newReels = List(5) { index ->
                Reel(
                    id = "reel_${UUID.randomUUID()}",
                    videoUrl = "https://example.com/video.mp4",
                    shortenUrl = "",
                    status = "PUBLISHED",
                    likeCount = 800,
                    commentCount = 90,
                    shareCount = 30,
                    viewCount = 3200,
                    createdAt = "2025-04-19T15:30:00Z",
                    thumbnailUrl = "https://example.com/thumbnail.gif",
                    tags = listOf("style", "kyobi")
                )
            }
            val currentSize = _reels.value.size
            _reels.value += newReels
            val latestSize = _reels.value.size
            Timber.tag(tag).d("Fetched more reels, latest size: $latestSize")
            preloadMediaSourceForRange(currentSize, latestSize)
            Timber.tag(tag).d("Preloaded additional media sources from page $currentSize to $latestSize")
        } finally {
            _isFetching.value = false
        }
    }

    private fun preloadMediaSourceForRange(startPage: Int, endPage: Int) {
        for (page in startPage until endPage) {
            if (page < _reels.value.size) {
                preloadShortenAndFullMediaSources(page)
            }
        }
    }

    private fun preloadShortenAndFullMediaSources(page: Int) {
        if (page >= reels.value.size) return
        val reel = reels.value[page]
        // Preload shortenUrl
        if (reel.shortenUrl.isNotEmpty()) {
            executorService.execute {
                try {
                    val mediaItem = MediaItem.fromUri(reel.shortenUrl).buildUpon()
                        .setMediaId(reel.shortenUrl).build()
                    startCreateMediaSource(mediaItem, shouldCache = true)
                    Timber.tag(tag).d("Preloaded shortenUrl MediaSource for page $page")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to preload shortenUrl MediaSource for page $page")
                }
            }
        }
        // Preload videoUrl
        if (reel.videoUrl.isNotEmpty()) {
            executorService.execute {
                try {
                    val mediaItem = MediaItem.fromUri(reel.videoUrl).buildUpon()
                        .setMediaId(reel.videoUrl).build()
                    startCreateMediaSource(mediaItem, shouldCache = false)
                    Timber.tag(tag).d("Preloaded videoUrl MediaSource for page $page")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to preload videoUrl MediaSource for page $page")
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun startCreateMediaSource(mediaItem: MediaItem, shouldCache: Boolean = true): MediaSource {
        try {
            val uri = mediaItem.localConfiguration?.uri.toString()
            Timber.tag(tag).d("Creating MediaSource for URI: $uri")
            // Chọn DataSource.Factory dựa trên shouldCache
            val dataSourceFactory = if (shouldCache) {
                mediaCache.createSharedCacheDataSourceFactory(context, mediaCache.getCache())
            } else {
                mediaCache.createNonCachedDataSourceFactory(context)
            }
            val path = uri.toUri().path ?: ""
            return if (path.endsWith(".m3u8")) {
                // Tạo DefaultHlsExtractorFactory và cấu hình
                val customExtractorFactory = DefaultHlsExtractorFactory().apply {
                    // Tắt kiểm tra codec không cần thiết Không parse codec nào (thay cho FLAG_DISABLE_CODECS)
                    // Không parse codec nào, tương đương FLAG_DISABLE_CODECS
                    experimentalSetCodecsToParseWithinGopSampleDependencies(0)
                    // Tùy chỉnh codec để parse sample dependencies Chỉ parse H.264 (tăng tốc seeking)
                    experimentalSetCodecsToParseWithinGopSampleDependencies(C.VIDEO_CODEC_FLAG_H264)
                }
                HlsMediaSource.Factory(dataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .setExtractorFactory(customExtractorFactory)
                    .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(3))
                    .createMediaSource(mediaItem)
            } else {
                ProgressiveMediaSource.Factory(dataSourceFactory)
                    .setLoadErrorHandlingPolicy(DefaultLoadErrorHandlingPolicy(3))
                    .createMediaSource(mediaItem)
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to create mediaSource")
            throw e
        }
    }

    @OptIn(UnstableApi::class)
    fun startPlay(targetPlayer: ExoPlayer?, page: Int) {
        targetPlayer?.setPriority(C.PRIORITY_PLAYBACK)
        targetPlayer?.repeatMode = Player.REPEAT_MODE_ALL
        targetPlayer?.playWhenReady = true
        targetPlayer?.play()
    }

    @OptIn(UnstableApi::class)
    fun startPause(targetPlayer: ExoPlayer?, page: Int) {
        targetPlayer?.setPriority(C.PRIORITY_PLAYBACK_PRELOAD)
        targetPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        targetPlayer?.playWhenReady = false
        targetPlayer?.pause()
        targetPlayer?.seekTo(0)
    }

    fun startRelease(targetPlayer: ExoPlayer?, page: Int) {
        targetPlayer?.playWhenReady = false
        targetPlayer?.repeatMode = Player.REPEAT_MODE_OFF
        targetPlayer?.pause()
        Timber.tag(tag).d("Pausing ExoPlayer for page $page before stop")
        targetPlayer?.stop()
        targetPlayer?.clearMediaItems()
        Timber.tag(tag).d("Stopping ExoPlayer for page $page before release")
        targetPlayer?.release()
        Timber.tag(tag).d("Releasing ExoPlayer for page $page")
    }

    override fun onCleared() {
        Timber.tag(tag).d("ViewModel cleared, releasing resources")
        // Shutdown ExecutorService
        try {
            executorService.shutdown()
            if (!executorService.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)) {
                executorService.shutdownNow()
                Timber.tag(tag).w("ExecutorService did not terminate gracefully, forced shutdown")
            }
            Timber.tag(tag).d("ExecutorService shut down successfully")
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to shutdown ExecutorService")
        }
        super.onCleared()
    }
}