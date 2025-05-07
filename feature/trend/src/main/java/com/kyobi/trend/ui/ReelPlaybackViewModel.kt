package com.kyobi.trend.ui

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
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
import javax.inject.Inject
import androidx.core.net.toUri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.hls.DefaultHlsExtractorFactory
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@HiltViewModel
class ReelPlaybackViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: MediaCache,
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private val _reels = mutableStateOf<List<Reel>>(emptyList())
    val reels: State<List<Reel>> = _reels
    private val _isFetching = mutableStateOf(false)
    val isFetching: State<Boolean> = _isFetching
    private val _mediaSources = mutableMapOf<String, MediaSource>()
    private var exoPlayer: ExoPlayer? = null
    private var currentSettledPage = 0
    private val _firstFrameRendered = MutableStateFlow(-1) // -1: chưa render
    val firstFrameRendered = _firstFrameRendered.asStateFlow()

    @OptIn(UnstableApi::class)
    fun initializePlayer(mediaSources: List<MediaSource>) {
        if (exoPlayer == null) {
            val renderersFactory = DefaultRenderersFactory(context)
                .setEnableDecoderFallback(false)
                .forceDisableMediaCodecAsynchronousQueueing()
            val cacheDataSourceFactory = mediaCache.getMediaSourceFactory(shouldCache = true)
            exoPlayer = ExoPlayer.Builder(context)
                .setRenderersFactory(renderersFactory)
                .setLoadControl(
                    DefaultLoadControl.Builder()
                        .setBufferDurationsMs(
                            10000, // minBufferMs: 10s, đủ preload shorten + full
                            30000, // maxBufferMs: 30s, preload vài page kế tiếp
                            2000,  // bufferForPlaybackMs: 2s, bắt đầu play nhanh
                            2000   // bufferForPlaybackAfterRebufferMs: 2s, ổn định sau rebuffer
                        )
                        .setTargetBufferBytes(-1)
                        .build()
                )
                .setMediaSourceFactory(cacheDataSourceFactory)
                .build().apply {
                    if (mediaSources.isNotEmpty()) {
                        setMediaSources(mediaSources, 0, 0)
                        seekTo(0, 0)
                        repeatMode = Player.REPEAT_MODE_ONE
                        videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                        volume = 1f
                        playWhenReady = false
                    }
                    addListener(object : Player.Listener {
                        override fun onRenderedFirstFrame() {
                            Timber.tag(tag).d("First frame rendered for page $currentSettledPage")
                            _firstFrameRendered.value = currentSettledPage
                        }
                        override fun onPositionDiscontinuity(
                            oldPosition: Player.PositionInfo,
                            newPosition: Player.PositionInfo,
                            reason: Int
                        ) {
                            if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                                Timber.tag(tag).d("Auto transition detected at page $currentSettledPage, periodIndex: ${newPosition.periodIndex}")
                                // Page hiện tại: shorten = 2 * page, full = 2 * page + 1
                                val fullPeriodIndex = 2 * currentSettledPage + 1 // Period của fullMediaSource
                                if (newPosition.periodIndex > fullPeriodIndex) {
                                    Timber.tag(tag).d("Looping back to page $currentSettledPage")
                                }
                            }
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            Timber.tag(tag).e(error, "Player error for page $currentSettledPage")
                        }
                        override fun onVideoSizeChanged(videoSize: VideoSize) {
                            Timber.tag(tag).d("Video size changed for page $currentSettledPage: ${videoSize.width}x${videoSize.height}")
                        }
                        override fun onPlaybackStateChanged(state: Int) {
                            Timber.tag(tag).d("Playback state changed for page $currentSettledPage: $state")
                        }
                        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                            Timber.tag(tag).d("Media item transition to mediaId ${mediaItem?.mediaId} for page $currentSettledPage")
                        }
                        override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
                            Timber.tag(tag).d("Audio attributes changed for page $currentSettledPage: contentType=${audioAttributes.contentType}, usage=${audioAttributes.usage}, flags=${audioAttributes.flags}")
                        }
                        override fun onAudioSessionIdChanged(audioSessionId: Int) {
                            Timber.tag(tag).d("Audio session ID changed for page $currentSettledPage: audioSessionId=$audioSessionId")
                        }
                        override fun onVolumeChanged(volume: Float) {
                            Timber.tag(tag).d("Volume changed for page $currentSettledPage: volume=$volume")
                        }
                        override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
                            Timber.tag(tag).d("Device volume changed for page $currentSettledPage: volume=$volume, muted=$muted")
                        }
                    })
                }
            Timber.tag(tag).d("Initialized single ExoPlayer instance")
        }
    }

    // Lấy ExoPlayer
    fun getPlayer(): ExoPlayer? = exoPlayer

    @OptIn(UnstableApi::class)
    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        _reels.value = newReels
        preloadMediaSourceForRange(0, newReels.size) // Preload trước
        // Tạo danh sách mergedSources
        val mergedSources = newReels.mapIndexed { index, reel ->
            val shortenMediaSource = _mediaSources[reel.shortenUrl]
                ?: throw IllegalStateException("Shorten MediaSource for ${reel.shortenUrl} not preloaded")
            val fullMediaSource = _mediaSources[reel.videoUrl]
                ?: throw IllegalStateException("Full MediaSource for ${reel.videoUrl} not preloaded")
            try {
                ConcatenatingMediaSource2.Builder()
                    .add(shortenMediaSource, 10_000L)
                    .add(fullMediaSource, 180_000L)
                    .build().also {
                        Timber.tag(tag).d("ConcatenatingMediaSource2 created for page $index")
                    }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to create ConcatenatingMediaSource2 for page $index")
                throw e
            }
        }
        initializePlayer(mergedSources) // init ExoPlayer with init mergedSources
        Timber.tag(tag).d("Preloaded and set ${newReels.size} media sources")
    }

    fun fetchMoreReels() {
        if (_isFetching.value) return
        _isFetching.value = true
        // Làm sau
        _isFetching.value = false
    }

    @OptIn(UnstableApi::class)
    fun updateSettledPage(page: Int, playerView: PlayerView) {
        currentSettledPage = page
        playerView.player = exoPlayer
    }

    private fun preloadMediaSourceForRange(startPage: Int, endPage: Int) {
        for (page in startPage until endPage) {
            if (page < _reels.value.size) {
                preloadShortenAndFullMediaSources(page)
            }
        }
    }

    private fun preloadShortenAndFullMediaSources(page: Int) {
        if (page >= _reels.value.size) return
        val reel = _reels.value[page]
        // Preload shortenUrl
        if (reel.shortenUrl.isNotEmpty()) {
            try {
                val mediaItem = MediaItem.fromUri(reel.shortenUrl).buildUpon()
                    .setMediaId(reel.shortenUrl).build()
                val source = startCreateMediaSource(mediaItem, shouldCache = true)
                _mediaSources[reel.shortenUrl] = source
                Timber.tag(tag).d("Preloaded shortenUrl MediaSource for page $page")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload shortenUrl MediaSource for page $page")
            }
        }
        // Preload videoUrl
        if (reel.videoUrl.isNotEmpty()) {
            try {
                val mediaItem = MediaItem.fromUri(reel.videoUrl).buildUpon()
                    .setMediaId(reel.videoUrl).build()
                val source = startCreateMediaSource(mediaItem, shouldCache = false)
                _mediaSources[reel.videoUrl] = source
                Timber.tag(tag).d("Preloaded videoUrl MediaSource for page $page")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload videoUrl MediaSource for page $page")
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
    fun startPlay(page: Int, playerView: PlayerView) {
        exoPlayer?.let { player ->
            player.seekTo(page, 0) // very important. giup giam first frame render
            player.prepare()
            player.playWhenReady = true
            Timber.tag(tag).d("Playing ExoPlayer for init page greater than 0")
        }
        playerView.player = exoPlayer
    }

    @OptIn(UnstableApi::class)
    fun startPause(page: Int, playerView: PlayerView) {
        exoPlayer?.let { player ->
            player.playWhenReady = false
            Timber.tag(tag).d("Paused ExoPlayer for page $page")
        }
        playerView.player = exoPlayer
    }

    private fun startRelease() {
        exoPlayer?.let { player ->
            player.seekTo(0)
            player.playWhenReady = false
            player.stop()
            player.clearMediaItems()
            player.release()
            Timber.tag(tag).d("Releasing ExoPlayer")
        }
    }

    override fun onCleared() {
        Timber.tag(tag).d("ViewModel cleared, releasing resources")
        startRelease()
        super.onCleared()
    }
}