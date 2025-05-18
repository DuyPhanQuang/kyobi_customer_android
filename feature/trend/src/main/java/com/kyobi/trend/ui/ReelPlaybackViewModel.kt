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
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import com.kyobi.trend.cache.ReelMediaCache
import com.kyobi.trend.model.Reel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
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
import com.kyobi.trend.cache.ReelPreloadManager
import com.kyobi.trend.extensions.addPerformanceTracker
import com.kyobi.trend.performance_metrics.AudioFocusManager
import com.kyobi.trend.performance_metrics.DEFAULT_VALUE
import com.kyobi.trend.performance_metrics.VideoPerformanceTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

const val SEEK_TO_DEFAULT_VALUE = 0L

@HiltViewModel
class ReelPlaybackViewModel
@OptIn(UnstableApi::class)
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: ReelMediaCache,
    private val reelPreloadManager: ReelPreloadManager
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private val _reels = mutableStateOf<List<Reel>>(emptyList())
    val reels: State<List<Reel>> = _reels
    private val _mediaSources = mutableMapOf<String, MediaSource>()
    private val _backgroundMediaSources = mutableMapOf<String, MediaSource>()
    private var mainExoPlayer: ExoPlayer? = null
    private var backgroundExoPlayer: ExoPlayer? = null
    private var currentSettledPage = 0
    private val _firstFrameRendered = MutableStateFlow(-1) // -1: chưa render
    val firstFrameRendered = _firstFrameRendered.asStateFlow()
    private val mainPlayerTracker = VideoPerformanceTracker()

    /** initiate main & background ExoPlayer instance
     *
     * update reels data and set media sources
     *
     * Only for first time case:
     *
     * set reels data -> preload all sources -> process background player -> process main player
     * */
    init {
        initializeMainPlayer()
        initializeBackgroundPlayer()
        viewModelScope.launch {
            reelPreloadManager.loadPreloadedUrls() // run on IO, nhưng trả về main thread
            setReelsAndPreloadAllSourcesThenProcessPlayer(mockData) // run on main thread
        }
    }

    @OptIn(UnstableApi::class)
    fun initializeMainPlayer() {
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
            .forceEnableMediaCodecAsynchronousQueueing()
        val cacheDataSourceFactory = mediaCache.getMediaSourceFactory(shouldCache = true)
        val loadControl = DefaultLoadControl.Builder()
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBufferDurationsMs(20000, 20000, 1000, 1000)
            .build()
        mainExoPlayer = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(cacheDataSourceFactory)
            .setUseLazyPreparation(false)
            .build().apply {
                setPriority(C.PRIORITY_PLAYBACK)
                repeatMode = Player.REPEAT_MODE_ONE
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                volume = 1f
                addPerformanceTracker(mainPlayerTracker)
                val audioFocusManager = AudioFocusManager(context)
                audioFocusManager.requestAudioFocus()
                addListener(object : Player.Listener {
                    override fun onRenderedFirstFrame() {
                        val result = mainPlayerTracker.getResult()
                        val firstFrameDurationMs = if (result.videoPerformanceData.loadTime.videoFirstFrameRenderedTimestamp != DEFAULT_VALUE &&
                            result.videoPerformanceData.loadTime.networkOrCacheVideoLoadingStartedTimestamp != DEFAULT_VALUE) {
                            result.videoPerformanceData.loadTime.videoFirstFrameRenderedTimestamp -
                                    result.videoPerformanceData.loadTime.networkOrCacheVideoLoadingStartedTimestamp
                        } else { 0L }
                        Timber.tag(tag).d("Main player performance: load_duration=${result.videoPerformanceData.loadTime.networkOrCacheVideoLoadingDurationMs}ms, " +
                                "first_frame=${firstFrameDurationMs}ms, " +
                                "video_decoder=${result.videoPerformanceData.decoders.videoDecoderName}, " +
                                "video_decoder_init=${result.videoPerformanceData.decoders.videoDecoderInitialisationDurationMs}ms")
                        mainPlayerTracker.invalidateSession()
                        _firstFrameRendered.value = currentSettledPage
                    }
                    override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                        if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                            Timber.tag(tag).d("Auto transition detected at page $currentSettledPage, periodIndex: ${newPosition.periodIndex}")
                            val fullPeriodIndex = 2 * currentSettledPage + 1
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
                        Timber.tag(tag).d("Audio attributes changed for page $currentSettledPage: contentType=${audioAttributes.contentType}, usage=${audioAttributes.usage}, flags=${audioAttributes.flags}, hasAudioTrack=${audioAttributes.contentType != C.AUDIO_CONTENT_TYPE_UNKNOWN}")
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
    }

    /** setUseLazyPreparation to `FALSE` -> very important -> reduce timing prepare sources and pre-warm renderer
     *
     * experimentalSetEnableMediaCodecVideoRendererPrewarming -> enable pre-warm renderer
     *
     * EXTENSION_RENDERER_MODE_OFF -> disabled audio renderer
     */
    @OptIn(UnstableApi::class)
    fun initializeBackgroundPlayer() {
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
            .forceEnableMediaCodecAsynchronousQueueing()
            .experimentalSetEnableMediaCodecVideoRendererPrewarming(true)
            .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        val cacheDataSourceFactory = mediaCache.getMediaSourceFactory(shouldCache = true)
        val loadControl = DefaultLoadControl.Builder()
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBufferDurationsMs(20000, 20000, 1000, 1000)
            .build()
        backgroundExoPlayer = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(cacheDataSourceFactory)
            .setUseLazyPreparation(false)
            .build().apply {
                setPriority(C.PRIORITY_PLAYBACK_PRELOAD)
                volume = 0f
                playWhenReady = false
            }
    }

    /** Only call at first time fetch reel data (api fetch reel page 1)
     * */
    @OptIn(UnstableApi::class)
    fun prepareAndStartVideoPage0() {
        mainPlayerTracker.invalidateSession()
        mainExoPlayer!!.prepare()
        mainExoPlayer!!.seekTo(0, SEEK_TO_DEFAULT_VALUE)
        mainExoPlayer!!.playWhenReady = true
    }

    /** Only call at first time fetch reel data (api fetch reel page 1)
     * */
    @OptIn(UnstableApi::class)
    private fun firstTimeProcessBackgroundPlayer(shortenSources: List<MediaSource>) {
        val startPreloadTimestamp = System.currentTimeMillis()
        fun preWarmPage(page: Int) {
            if (page >= shortenSources.size) {
                val preloadDurationMs = System.currentTimeMillis() - startPreloadTimestamp
                Timber.tag(tag).d("Background pre warm completed in ${preloadDurationMs}ms, prepare and start video at page 0")
                prepareAndStartVideoPage0()
                return
            }
            val shortenUrl = _reels.value[page].shortenUrl
            viewModelScope.launch {
                try {
                    val isPreloaded = reelPreloadManager.isPreloadedAndCached(shortenUrl)
                    if (!isPreloaded) {
                        backgroundExoPlayer!!.seekTo(page, SEEK_TO_DEFAULT_VALUE)
                        backgroundExoPlayer!!.prepare()
                        Timber.tag(tag).d("Background page $page is pre warming")
                        backgroundExoPlayer!!.addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_READY) {
                                    Timber.tag(tag).d("Background page $page playback state: STATE_READY")
                                    viewModelScope.launch {
                                        reelPreloadManager.savePreloadedMedia(shortenUrl)
                                    }
                                    backgroundExoPlayer!!.removeListener(this)
                                    preWarmPage(page + 1)
                                }
                            }
                            override fun onPlayerError(error: PlaybackException) {
                                Timber.tag(tag).e(error, "Background page $page player error")
                                backgroundExoPlayer!!.removeListener(this)
                                preWarmPage(page + 1)
                            }
                        })
                    } else {
                        Timber.tag(tag).d("URL already preloaded for page $page")
                        preWarmPage(page + 1)
                    }
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to pre warm HLS for page $page")
                    preWarmPage(page + 1)
                }
            }
        }
        preWarmPage(0)
    }

    fun getMainPlayer(): ExoPlayer? = mainExoPlayer

    /** Only call at first time fetch reel data (api fetch reel page 1)
     *
     * Step1: Update reels data
     *
     * Step2: preload all sources (shorten & full)
     *
     * Step3: set media sources for background player and main player
     *
     * Step4: start processing background player
     *
     * Step 5: start processing main player
     * */
    @OptIn(UnstableApi::class)
    fun setReelsAndPreloadAllSourcesThenProcessPlayer(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        _reels.value = newReels
        viewModelScope.launch {
            val preloadResult = preloadMediaSourceForRange(0, newReels.size)
            if (!preloadResult.isSuccess) return@launch
            val shortenSources = newReels.mapIndexedNotNull { _, reel ->
                val backgroundSource = _backgroundMediaSources[reel.shortenUrl]!!
                val isCached = reelPreloadManager.isPreloadedAndCached(reel.shortenUrl)
                if (!isCached) backgroundSource else return@mapIndexedNotNull null
            }
            val mergedSources = newReels.mapIndexedNotNull { index, reel ->
                val shortenMediaSource = _mediaSources[reel.shortenUrl]!!
                val fullMediaSource = _mediaSources[reel.videoUrl]!!
                val shortenDurationConfig = reel.shortenDuration
                val fullDurationConfig = reel.originalDuration - shortenDurationConfig
                try {
                    val shortenDurationMs = (shortenDurationConfig * 1000).toLong()
                    val fullDurationMs = (fullDurationConfig * 1000).toLong()
                    ConcatenatingMediaSource2.Builder()
                        .add(shortenMediaSource, shortenDurationMs)
                        .add(fullMediaSource, fullDurationMs)
                        .build().also {
                            Timber.tag(tag).d("ConcatenatingMediaSource2 created for page $index")
                        }
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to create ConcatenatingMediaSource2 for page $index")
                    return@mapIndexedNotNull null
                }
            }
            if (backgroundExoPlayer == null || mainExoPlayer == null) {
                return@launch
            }
            if (shortenSources.isNotEmpty()) {
                backgroundExoPlayer!!.setMediaSources(shortenSources, 0, SEEK_TO_DEFAULT_VALUE)
            }
            if (mergedSources.isNotEmpty()) {
                mainExoPlayer!!.setMediaSources(mergedSources, 0, SEEK_TO_DEFAULT_VALUE)
            }
            if (shortenSources.isNotEmpty()) {
                firstTimeProcessBackgroundPlayer(shortenSources)
            } else if (mergedSources.isNotEmpty()) {
                prepareAndStartVideoPage0()
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun updateSettledPage(page: Int, playerView: PlayerView) {
        currentSettledPage = page
        mainPlayerTracker.invalidateSession()
        playerView.player = mainExoPlayer
    }

    private fun preloadMediaSourceForRange(startPage: Int, endPage: Int): Result<Unit> {
        return runCatching {
            for (page in startPage until endPage) {
                preloadShortenAndFullMediaSources(page)
            }
            Timber.tag(tag).d("Successfully preloaded media sources from page $startPage to ${endPage - 1}")
        }.onFailure { e ->
            Timber.tag(tag).e(e, "Failed to preload media sources for range $startPage to ${endPage - 1}")
        }
    }

    @OptIn(UnstableApi::class)
    private fun createMediaItem(url: String): MediaItem {
        val mediaItem = MediaItem.fromUri(url).buildUpon()
            .setMediaId(url)
            .build()
        return mediaItem
    }

    @OptIn(UnstableApi::class)
    private fun preloadShortenAndFullMediaSources(page: Int) {
        if (page >= _reels.value.size) return
        val reel = _reels.value[page]
        // Preload shortenUrl (mainPlayer backgroundPlayer)
        if (reel.shortenUrl.isNotEmpty()) {
            try {
                val mediaItem = createMediaItem(reel.shortenUrl)
                val mainSource = startCreateMediaSource(
                    uri = reel.shortenUrl,
                    mediaItem,
                    shouldCache = true)
                _mediaSources[reel.shortenUrl] = mainSource
                val backgroundSource = startCreateMediaSource(
                    uri = reel.shortenUrl,
                    mediaItem,
                    shouldCache = true)
                _backgroundMediaSources[reel.shortenUrl] = backgroundSource
                Timber.tag(tag).d("Preloaded shortenUrl MediaSource for page $page")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload shortenUrl MediaSource for page $page")
                throw e
            }
        }
        // Preload videoUrl (mainPlayer)
        if (reel.videoUrl.isNotEmpty()) {
            try {
                val mediaItem = createMediaItem(reel.videoUrl)
                val source = startCreateMediaSource(
                    uri = reel.videoUrl,
                    mediaItem,
                    shouldCache = false)
                _mediaSources[reel.videoUrl] = source
                Timber.tag(tag).d("Preloaded videoUrl MediaSource for page $page")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload videoUrl MediaSource for page $page")
                throw e
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun startCreateMediaSource(uri: String, mediaItem: MediaItem, shouldCache: Boolean = true): MediaSource {
        try {
            val dataSourceFactory = if (shouldCache) {
                mediaCache.createSharedCacheDataSourceFactory(context, mediaCache.getCache())
            } else {
                mediaCache.createNonCachedDataSourceFactory(context)
            }
            val path = uri.toUri().path!!
            return if (path.endsWith(".m3u8")) {
                // Tạo DefaultHlsExtractorFactory và cấu hình
                val customExtractorFactory = DefaultHlsExtractorFactory().apply {
                    // Tắt kiểm tra codec không cần thiết, Không parse codec nào
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
                throw Exception()
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to create mediaSource")
            throw e
        }
    }

    /** check xem player đã set MediaSource và prepared chưa.
     *
     * cần có logic check này bởi vì lần đầu render ui video players thì page index 0 rendered nhưng mà processMainPlayer chưa dc call
     *
     * important: sử dụng seekTo(page, 0) -> 0: giảm thời gian render first frame
     * */
    @OptIn(UnstableApi::class)
    fun seekToPageAndPlayIfNeeded(page: Int, playerView: PlayerView) {
        mainExoPlayer?.let { player ->
            Timber.tag(tag).w("startPlay called for page $page, mediaItemCount: ${player.mediaItemCount}, playbackState: ${player.playbackState}")
            if (player.mediaItemCount > 0 && player.playbackState != Player.STATE_IDLE) {
                player.seekTo(page, SEEK_TO_DEFAULT_VALUE)
                if (!player.isPlaying) {
                    player.playWhenReady = true
                }
            }
        }
        playerView.player = mainExoPlayer
    }

    fun startPlay(playerView: PlayerView) {
        mainExoPlayer?.let { player ->
            player.playWhenReady = true
        }
        playerView.player = mainExoPlayer
    }

    @OptIn(UnstableApi::class)
    fun startPause(playerView: PlayerView) {
        mainExoPlayer?.let { player ->
            player.playWhenReady = false
        }
        playerView.player = mainExoPlayer
    }

    private fun startMainRelease() {
        _mediaSources.clear()
        AudioFocusManager(context).abandonAudioFocus()
        mainExoPlayer!!.seekTo(SEEK_TO_DEFAULT_VALUE)
        mainExoPlayer!!.playWhenReady = false
        mainExoPlayer!!.stop()
        mainExoPlayer!!.clearMediaItems()
        mainExoPlayer!!.release()
        mainExoPlayer = null
    }

    private fun startBackgroundRelease() {
        _backgroundMediaSources.clear()
        backgroundExoPlayer!!.stop()
        backgroundExoPlayer!!.clearMediaItems()
        backgroundExoPlayer!!.release()
        backgroundExoPlayer = null
    }

    @OptIn(UnstableApi::class)
    override fun onCleared() {
        Timber.tag(tag).d("ViewModel cleared, releasing resources")
        startMainRelease()
        startBackgroundRelease()
        mediaCache.release()
        super.onCleared()
    }
}