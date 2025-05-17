package com.kyobi.trend.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class ReelPlaybackViewModel
@OptIn(UnstableApi::class)
@Inject
constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: MediaCache,
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

    init {
        // init instance mainExoPlayer & backgroundExoPlayer
        initializeMainPlayer()
        initializeBackgroundPlayer()
        // update reels data and set media sources
        viewModelScope.launch {
            reelPreloadManager.loadPreloadedUrls() // run on IO, nhưng trả về main thread
            setReels(mockData) // run on main thread
        }
    }

    @OptIn(UnstableApi::class)
    fun initializeMainPlayer() {
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(false)
            .forceDisableMediaCodecAsynchronousQueueing()
        val cacheDataSourceFactory = mediaCache.getMediaSourceFactory(shouldCache = true)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(10000, 30000, 2000, 2000)
            .setTargetBufferBytes(-1)
            .build()
        mainExoPlayer = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(cacheDataSourceFactory)
            .build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                volume = 1f
            }
    }

    @OptIn(UnstableApi::class)
    fun initializeBackgroundPlayer() {
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(false)
            .forceDisableMediaCodecAsynchronousQueueing()
        val cacheDataSourceFactory = mediaCache.getMediaSourceFactory(shouldCache = true)
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(10000, 30000, 2000, 2000)
            .setTargetBufferBytes(-1)
            .build()
        backgroundExoPlayer = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(cacheDataSourceFactory)
            .build().apply {
                volume = 0f
            }
    }

    @OptIn(UnstableApi::class)
    fun processMainPlayer(mediaSources: List<MediaSource>) {
        mainExoPlayer?.let { player ->
            if (mediaSources.isNotEmpty()) {
                player.setMediaSources(mediaSources, 0, 0)
                player.seekTo(0, 0)
                player.prepare()
                player.playWhenReady = true
            }
            player.addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    Timber.tag(tag).d("First frame rendered for page $currentSettledPage")
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
            Timber.tag(tag).d("Set media sources for main ExoPlayer")
        } ?: Timber.tag(tag).e("Main ExoPlayer is null")
    }

    @OptIn(UnstableApi::class)
    private fun processBackgroundPlayer(shortenSources: List<MediaSource>, mergedSources: List<MediaSource>) {
        if (backgroundExoPlayer == null) {
            Timber.tag(tag).e("Background ExoPlayer is null")
            return
        }
        backgroundExoPlayer!!.setMediaSources(shortenSources)
        backgroundExoPlayer!!.volume = 0f
        fun preloadPage(page: Int) {
            if (page >= shortenSources.size) {
                Timber.tag("ProcessBackground").d("Background preload completed, processing main player")
                processMainPlayer(mergedSources)
                return
            }
            val url = _reels.value[page].shortenUrl
            val localTag = "ProcessBackground"
            viewModelScope.launch {
                try {
                    val isPreloaded = reelPreloadManager.isPreloadedAndCached(url)
                    if (!isPreloaded) {
                        val cacheKey = reelPreloadManager.generateCacheKey(url)
                        backgroundExoPlayer!!.seekTo(page, 0)
                        backgroundExoPlayer!!.prepare()
                        backgroundExoPlayer!!.playWhenReady = true
                        Timber.tag(localTag).d("Background play for page $page, url=$url")
                        backgroundExoPlayer!!.addListener(object : Player.Listener {
                            override fun onPlaybackStateChanged(state: Int) {
                                if (state == Player.STATE_READY) {
                                    viewModelScope.launch {
                                        reelPreloadManager.savePreloadedMedia(url)
                                    }
                                    val cachedLength = mediaCache.getCache().getCachedLength(cacheKey, 0L, Long.MAX_VALUE)
                                    val cacheKeys = mediaCache.getCache().keys
                                    Timber.tag(localTag).d(
                                        "Preloaded HLS for page $page, url=$url, cacheKey=$cacheKey, " +
                                                "cachedLength=$cachedLength bytes, cacheKeys=${cacheKeys.joinToString()}")
                                    backgroundExoPlayer!!.playWhenReady = false
                                    backgroundExoPlayer!!.removeListener(this)
                                    preloadPage(page + 1)
                                }
                            }
                            override fun onPlayerError(error: PlaybackException) {
                                Timber.tag(localTag).e(error, "Background preload error for page $page, url=$url")
                                backgroundExoPlayer!!.playWhenReady = false
                                backgroundExoPlayer!!.removeListener(this)
                                preloadPage(page + 1)
                            }
                            override fun onIsLoadingChanged(isLoading: Boolean) {
                                Timber.tag(localTag).d("Loading state for page $page, url=$url, isLoading=$isLoading")
                            }
                        })
                    } else {
                        Timber.tag(localTag).d("URL already preloaded for page $page, url=$url")
                        preloadPage(page + 1)
                    }
                } catch (e: Exception) {
                    Timber.tag(localTag).e(e, "Failed to preload HLS for page $page, url=$url")
                    preloadPage(page + 1)
                }
            }
        }
        preloadPage(0)
    }

    fun getPlayer(): ExoPlayer? = mainExoPlayer

    @OptIn(UnstableApi::class)
    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        _reels.value = newReels
        preloadMediaSourceForRange(0, newReels.size) // Preload trước
        viewModelScope.launch {
            val shortenSources = mutableListOf<MediaSource>()
            val mergedSources = newReels.mapIndexedNotNull { index, reel ->
                // Fallback nếu media source không tồn tại
                if (!_mediaSources.containsKey(reel.shortenUrl) || !_mediaSources.containsKey(reel.videoUrl)) {
                    Timber.tag(tag).w("Media sources missing for ${reel.shortenUrl} or ${reel.videoUrl}, preloading")
                    preloadMediaSourceForRange(index, index + 1)
                }
                val shortenMediaSource = _mediaSources[reel.shortenUrl]
                val fullMediaSource = _mediaSources[reel.videoUrl]
                if (shortenMediaSource == null || fullMediaSource == null) {
                    Timber.tag(tag).e("Failed to get media sources for page $index")
                    return@mapIndexedNotNull null
                }
                val isCached = reelPreloadManager.isPreloadedAndCached(reel.shortenUrl)
                if (!isCached) {
                    val backgroundSource = _backgroundMediaSources[reel.shortenUrl]
                    if (backgroundSource == null) {
                        Timber.tag(tag).e("Background source missing for ${reel.shortenUrl} at page $index")
                        return@mapIndexedNotNull null
                    }
                    shortenSources.add(backgroundSource)
                }
                try {
                    ConcatenatingMediaSource2.Builder()
                        .add(shortenMediaSource, 10_000L)
                        .add(fullMediaSource, 180_000L)
                        .build().also {
                            Timber.tag(tag).d("ConcatenatingMediaSource2 created for page $index")
                        }
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to create ConcatenatingMediaSource2 for page $index")
                    return@mapIndexedNotNull null
                }
            }
            // chỉ call processBackgroundPlayer nếu có shortenSources
            if (shortenSources.isNotEmpty()) {
                processBackgroundPlayer(shortenSources, mergedSources)
            } else {
                processMainPlayer(mergedSources)
            }
            Timber.tag(tag).d("Preloaded and set ${newReels.size} media sources")
        }
    }

    @OptIn(UnstableApi::class)
    fun updateSettledPage(page: Int, playerView: PlayerView) {
        currentSettledPage = page
        playerView.player = mainExoPlayer
    }

    private fun preloadMediaSourceForRange(startPage: Int, endPage: Int) {
        for (page in startPage until endPage) {
            if (page < _reels.value.size) {
                preloadShortenAndFullMediaSources(page)
            }
        }
    }

    // sử dụng setCustomCacheKey cho case the link is expired, it can still be played.
    @OptIn(UnstableApi::class)
    private fun createMediaItem(url: String): MediaItem {
        val cacheKey = reelPreloadManager.generateCacheKey(url)
        val mediaItem = MediaItem.fromUri(url).buildUpon()
            .setMediaId(url)
            .setCustomCacheKey(cacheKey)
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
                val mainSource = startCreateMediaSource(mediaItem, shouldCache = true)
                _mediaSources[reel.shortenUrl] = mainSource
                val backgroundSource = startCreateMediaSource(mediaItem, shouldCache = true)
                _backgroundMediaSources[reel.shortenUrl] = backgroundSource
                Timber.tag(tag).d("Preloaded shortenUrl MediaSource for page $page")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload shortenUrl MediaSource for page $page")
            }
        }
        // Preload videoUrl (mainPlayer)
        if (reel.videoUrl.isNotEmpty()) {
            try {
                val mediaItem = createMediaItem(reel.videoUrl)
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
        mainExoPlayer?.let { player ->
            // check xem player đã set MediaSource và prepared chưa.
            // cần có logic check này bởi vì lần đầu render ui video players thì page index 0 rendered
            // nhưng mà processMainPlayer chưa dc call
            if (player.mediaItemCount > 0 && player.playbackState != Player.STATE_IDLE) {
                player.seekTo(page, 0) // Giảm thời gian render first frame
                player.playWhenReady = true
                Timber.tag(tag).d("Playing ExoPlayer for page $page")
            } else {
                Timber.tag(tag).w("startPlay called but player not ready for page $page, mediaItemCount: ${player.mediaItemCount}, playbackState: ${player.playbackState}")
            }
        } ?: Timber.tag(tag).e("Main ExoPlayer is null")
        playerView.player = mainExoPlayer
    }

    @OptIn(UnstableApi::class)
    fun startPause(page: Int, playerView: PlayerView) {
        mainExoPlayer?.let { player ->
            player.playWhenReady = false
            Timber.tag(tag).d("Paused ExoPlayer for page $page")
        }
        playerView.player = mainExoPlayer
    }

    private fun startMainRelease() {
        mainExoPlayer?.let { player ->
            player.seekTo(0)
            player.playWhenReady = false
            player.stop()
            player.clearMediaItems()
            player.release()
            Timber.tag(tag).d("Releasing Main ExoPlayer")
        }
        mainExoPlayer = null
        _mediaSources.clear()
    }

    private fun startBackgroundRelease() {
        backgroundExoPlayer?.let { player ->
            player.stop()
            player.clearMediaItems()
            player.release()
            Timber.tag(tag).d("Releasing Background ExoPlayer")
        }
        backgroundExoPlayer = null
        _backgroundMediaSources.clear()
        Handler(Looper.getMainLooper()).removeCallbacksAndMessages(null)
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