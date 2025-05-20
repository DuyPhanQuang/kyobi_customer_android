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
import com.kyobi.trend.cache.ReelPreloadManager
import com.kyobi.trend.extensions.addPerformanceTracker
import com.kyobi.trend.performance_metrics.AudioFocusManager
import com.kyobi.trend.performance_metrics.DEFAULT_VALUE
import com.kyobi.trend.performance_metrics.VideoPerformanceTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.ceil
import kotlin.math.min

const val SEEK_TO_DEFAULT_VALUE = 0L
const val PRELOAD_MAX_NEXT_PAGE_VALUE = 1

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
    private val _firstFrameRenderedPage = MutableStateFlow(-1) // -1: chưa render
    val firstFrameRenderedPage = _firstFrameRenderedPage.asStateFlow()
    private val _updateThumbnailPage0 = MutableStateFlow(-1)
    val updateThumbnailPage0 = _updateThumbnailPage0.asStateFlow()
    private val mainPlayerTracker = VideoPerformanceTracker()
    private val preWarmedPages = mutableSetOf<Int>()
    private val fetchSizes = mutableListOf<Int>()
    private val _isVideoProcessing = MutableStateFlow(true) // thể hiện show ui loading
    val isVideoProcessing = _isVideoProcessing.asStateFlow() // thể hiện show ui loading
    private val _isAllowUserScrollEnabled = MutableStateFlow(true)
    val isAllowUserScrollEnabled = _isAllowUserScrollEnabled.asStateFlow()

    /** initiate main & background ExoPlayer instance
     *
     * update reels data and set media sources
     *
     * Only for first time case:
     *
     * case1: chưa có shorten sources nào trong possible range được preload (tức shortenSources = notEmpty):
     * set reels data -> preload all sources -> process background player -> process main player
     *
     * case2: shorten sources nào trong possible range đã preloaded trước đó:
     * set reels data -> preload all sources -> process main player
     * */
    init {
        initializeMainPlayer()
        initializeBackgroundPlayer()
        viewModelScope.launch {
            reelPreloadManager.loadPreloadedUrls() // run on IO, nhưng trả về main thread
            setReelsAndPreloadAllSourcesThenProcessPlayer(mockData) // run on main thread
        }
    }

    /** experimentalSetEnableMediaCodecVideoRendererPrewarming -> enable pre-warm renderer
     *
     * Giảm độ trễ khi chuyển media item, phù hợp với playback liên tục
     * */
    @OptIn(UnstableApi::class)
    fun initializeMainPlayer() {
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
            .forceEnableMediaCodecAsynchronousQueueing()
        val cacheDataSourceFactory = mediaCache.getMediaSourceFactory(shouldCache = true)
        val loadControl = DefaultLoadControl.Builder()
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBufferDurationsMs(20000, 20000, 1000, 2000)
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
                    }
                    override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                            val oldPage = oldPosition.mediaItemIndex
                            val newPage = newPosition.mediaItemIndex
                            Timber.tag(tag).d("Position discontinuity from page $oldPage to $newPage, reason: $reason")
                            // chỉ trigger khi tiến tới và page + `PRELOAD_MAX_NEXT_PAGE_VALUE` chưa pre warm
                            val possibleNextPage = newPage + PRELOAD_MAX_NEXT_PAGE_VALUE
                            if (newPage > oldPage && !preWarmedPages.contains(possibleNextPage)) {
                                Timber.tag(tag).d("Trigger pre warm for page $possibleNextPage")
                                preWarmNextPages(possibleNextPage)
                            }
                        } else if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                            Timber.tag(tag).d("Auto transition at page $currentSettledPage, periodIndex: ${newPosition.periodIndex}")
                            val totalMediaPeriodOnMergedSource = 2
                            val fullPeriodIndex = totalMediaPeriodOnMergedSource * currentSettledPage + 1
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
                        val url = mediaItem?.localConfiguration?.uri.toString()
                        Timber.tag(tag).d("Media item transition to mediaId ${mediaItem?.mediaId} url: $url for page $currentSettledPage")
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
     */
    @OptIn(UnstableApi::class)
    fun initializeBackgroundPlayer() {
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true)
            .forceEnableMediaCodecAsynchronousQueueing()
        val cacheDataSourceFactory = mediaCache.getMediaSourceFactory(shouldCache = true)
        val loadControl = DefaultLoadControl.Builder()
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBufferDurationsMs(20000, 20000, 5000, 10000)
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

    fun getMainPlayer(): ExoPlayer? = mainExoPlayer

    /** Only call at first time fetch reel data (api fetch reel page 1)
     * */
    @OptIn(UnstableApi::class)
    private fun firstTimeProcessBackgroundPlayer(shortenSources: List<MediaSource>) {
        val startPreloadTimestamp = System.currentTimeMillis()
        _isVideoProcessing.value = true
        fun preWarmPage(page: Int) {
            val isPreWarmCompleted = firstTimeHasPreWarmCompleted(page, maxPage = shortenSources.size)
            if (isPreWarmCompleted) {
                val preloadDurationMs = System.currentTimeMillis() - startPreloadTimestamp
                Timber.tag(tag).d("Background pre warm completed in ${preloadDurationMs}ms, prepare and start video at page 0")
                _isVideoProcessing.value = false
                prepareAndStartVideoPage0()
                return
            }
            val shortenUrl = _reels.value[page].shortenUrl
            val nextPage = page + 1
            processingPreWarmSinglePage(page, shortenUrl, backgroundExoPlayer!!) {
                preWarmPage(nextPage)
            }
        }
        preWarmPage(1) // Bắt đầu từ page 1
    }

    /** Only call at first time fetch reel data (api fetch reel page 1)
     *
     * `case1`: chưa có shorten sources nào trong possible range được preload (tức shortenSources = notEmpty):
     * set reels data -> preload all sources -> process background player -> process main player
     *
     * `case2`: shorten sources nào trong possible range đã preloaded trước đó:
     * set reels data -> preload all sources -> process main player
     *
     * Step1: Update reels data và save size của lần fetch đầu tiên
     *
     * Step2: preload all sources (shorten & full)
     *
     * Step3: set media sources for background player and main player
     *
     * Step4: start processing background player if needed (xem chú thích ở `case1` and `case2`)
     *
     * Step 5: start processing main player
     * */
    @OptIn(UnstableApi::class)
    fun setReelsAndPreloadAllSourcesThenProcessPlayer(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        _reels.value = newReels
        fetchSizes.add(newReels.size)
        viewModelScope.launch {
            val preloadResult = preloadMediaSourceForRange(0, newReels.size)
            if (!preloadResult.isSuccess) return@launch
            val (shortenSources, mergedSources) = createShortenSourcesAndMergeSources(newReels, baseIndex = 0)
            if (backgroundExoPlayer == null || mainExoPlayer == null) {
                Timber.tag(tag).w("Missing backgroundExoPlayer or mainExoPlayer")
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

    /** Only call at first time fetch reel data (api fetch reel page 1)
     *
     * `prepare()` của main player chỉ nên call ở case sau:
     *
     * First time sau khi background player pre-warm cho possible pages done
     * */
    @OptIn(UnstableApi::class)
    fun prepareAndStartVideoPage0() {
        val prepareAndStartVideoPage0Tag = "prepareAndStartVideoPage0"
        val page0 = 0
        mainPlayerTracker.invalidateSession()
        val listener = object : Player.Listener {
            var localSeekCompleted = false
            var localFirstFrameRendered = false
            override fun onRenderedFirstFrame() {
                if (currentSettledPage == page0) {
                    Timber.tag(prepareAndStartVideoPage0Tag).d("First frame rendered for page $page0")
                    localFirstFrameRendered = true
                    tryAttachAndPlay()
                }
            }
            override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                if (reason == Player.DISCONTINUITY_REASON_SEEK) {
                    Timber.tag(prepareAndStartVideoPage0Tag).d("Seek to page $page0 completed")
                    localSeekCompleted = true
                    tryAttachAndPlay()
                }
            }
            private fun tryAttachAndPlay() {
                if (localSeekCompleted && localFirstFrameRendered) {
                    Timber.tag(prepareAndStartVideoPage0Tag).d("Attaching playerView and playing for page $page0")
                    _updateThumbnailPage0.value = page0
                    _isAllowUserScrollEnabled.value = true
                    if (!mainExoPlayer!!.isPlaying) {
                        mainExoPlayer!!.playWhenReady = true
                    }
                    mainExoPlayer!!.removeListener(this)
                }
            }
        }
        mainExoPlayer!!.addListener(listener)
        mainExoPlayer!!.seekTo(page0, SEEK_TO_DEFAULT_VALUE)
        mainExoPlayer!!.prepare()
    }

    /** Only call at first time fetch reel data (api fetch reel page 1)
     *
     * pre warm next pages: current + 1
     * */
    private fun firstTimeHasPreWarmCompleted(page: Int, maxPage: Int): Boolean {
        return page >= min(maxPage, PRELOAD_MAX_NEXT_PAGE_VALUE + 1)
    }

    private fun isOutOfRange(page: Int, maxPage: Int): Boolean {
        return page >= maxPage || page >= _reels.value.size || preWarmedPages.contains(page)
    }

    /** `prepare()` của background player nên called cho mỗi lần pre warm page
     *
     * pre warm tối đa `PRELOAD_MAX_NEXT_PAGE_VALUE` page
     * */
    @OptIn(UnstableApi::class)
    private fun preWarmNextPages(startPage: Int) {
        val maxPage = min(startPage + PRELOAD_MAX_NEXT_PAGE_VALUE, _reels.value.size)
        fun preWarmPage(page: Int) {
            if (isOutOfRange(page, maxPage)) {
                Timber.tag(tag).d("Skip pre warm page $page: out of range or already pre warmed")
                return
            }
            val shortenUrl = _reels.value[page].shortenUrl
            val nextPage = page + 1
            processingPreWarmSinglePage(page, shortenUrl, backgroundExoPlayer!!) {
                preWarmPage(nextPage)
            }
        }
        preWarmPage(startPage)
    }

    /** xử lý preload và tracking cache if needed */
    @OptIn(UnstableApi::class)
    private suspend fun handlePreloadAndCache(shortenUrl: String): Boolean {
        try {
            reelPreloadManager.savePreloadedMedia(shortenUrl)
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /** `prepare()` của background player nên called cho mỗi lần pre warm page
     * */
    @OptIn(UnstableApi::class)
    private fun processingPreWarmSinglePage(
        page: Int,
        shortenUrl: String,
        backgroundExoPlayer: ExoPlayer,
        onPreWarmCompleted: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val isPreloaded = reelPreloadManager.isPreloadedAndCached(shortenUrl)
                if (!isPreloaded && !preWarmedPages.contains(page)) {
                    backgroundExoPlayer.seekTo(page, SEEK_TO_DEFAULT_VALUE)
                    backgroundExoPlayer.prepare()
                    Timber.tag(tag).d("Background page $page is pre warming")
                    var listener: Player.Listener? = null
                    listener = object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            if (state == Player.STATE_READY) {
                                Timber.tag(tag).d("Background page $page playback state: STATE_READY")
                                viewModelScope.launch {
                                    val preloadedResult = handlePreloadAndCache(shortenUrl)
                                    if (preloadedResult) {
                                        preWarmedPages.add(page)
                                        listener?.let { backgroundExoPlayer.removeListener(it) }
                                        onPreWarmCompleted()
                                    }
                                }
                            }
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            Timber.tag(tag).e(error, "Background page $page player error")
                            backgroundExoPlayer.removeListener(this)
                            onPreWarmCompleted()
                        }
                    }
                    backgroundExoPlayer.addListener(listener)
                } else {
                    Timber.tag(tag).d("URL already preloaded or pre warmed for page $page")
                    onPreWarmCompleted()
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to pre warm HLS for page $page")
                onPreWarmCompleted()
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun updateSettledPage(page: Int) {
        currentSettledPage = page
        // Trigger load more tại nửa số item(future reels size / 2) của lần fetch cuối
        val reelSize = _reels.value.size
        val lastFetchSize = fetchSizes.lastOrNull() ?: 0
        if (lastFetchSize > 0 && page == reelSize - lastFetchSize / 2) {
            Timber.tag(tag).d("Trigger load more at page $page, last fetch size: $lastFetchSize")
            loadMoreReels()
        }
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

    /** Preload shortenUrl cho `mainPlayer` và `backgroundPlayer`
     *
     * Preload videoUrl cho `mainPlayer`
     * */
    @OptIn(UnstableApi::class)
    private fun preloadShortenAndFullMediaSources(page: Int) {
        if (page >= _reels.value.size) return
        val reel = _reels.value[page]
        if (reel.shortenUrl.isNotEmpty()) {
            try {
                val mediaItem = createMediaItem(reel.shortenUrl)
                val mainSource = startCreateMediaSource(uri = reel.shortenUrl, mediaItem, shouldCache = true)
                _mediaSources[reel.shortenUrl] = mainSource
                val backgroundSource = startCreateMediaSource(uri = reel.shortenUrl, mediaItem, shouldCache = true)
                _backgroundMediaSources[reel.shortenUrl] = backgroundSource
                Timber.tag(tag).d("Preloaded shortenUrl MediaSource for page $page")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload shortenUrl MediaSource for page $page")
                throw e
            }
        }
        if (reel.videoUrl.isNotEmpty()) {
            try {
                val mediaItem = createMediaItem(reel.videoUrl)
                val source = startCreateMediaSource(uri = reel.videoUrl, mediaItem, shouldCache = false)
                _mediaSources[reel.videoUrl] = source
                Timber.tag(tag).d("Preloaded videoUrl MediaSource for page $page")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload videoUrl MediaSource for page $page")
                throw e
            }
        }
    }

    @OptIn(UnstableApi::class)
    private fun createMediaItem(url: String): MediaItem {
        return MediaItem.fromUri(url).buildUpon().setMediaId(url).build()
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

    @OptIn(UnstableApi::class)
    private suspend fun createShortenSourcesAndMergeSources(reels: List<Reel>, baseIndex: Int): Pair<List<MediaSource>, List<MediaSource>> {
        val shortenSources = reels.mapIndexedNotNull { _, reel ->
            val backgroundSource = _backgroundMediaSources[reel.shortenUrl]!!
            val isCached = reelPreloadManager.isPreloadedAndCached(reel.shortenUrl)
            if (!isCached) backgroundSource else null
        }
        val mergedSources = reels.mapIndexedNotNull { index, reel ->
            val shortenMediaSource = _mediaSources[reel.shortenUrl]!!
            val fullMediaSource = _mediaSources[reel.videoUrl]!!
            val shortenDurationConfig = reel.shortenDuration
            val fullDurationConfig = reel.originalDuration - shortenDurationConfig
            try {
                val shortenDurationMs = ceil(shortenDurationConfig * 1000).toLong()
                val fullDurationMs = ceil(fullDurationConfig * 1000).toLong()
                ConcatenatingMediaSource2.Builder()
                    .add(shortenMediaSource, shortenDurationMs)
                    .add(fullMediaSource, fullDurationMs)
                    .build().also {
                        Timber.tag(tag).d("ConcatenatingMediaSource2 created for page ${baseIndex + index}")
                    }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to create ConcatenatingMediaSource2 for page ${baseIndex + index}")
                null
            }
        }
        return shortenSources to mergedSources
    }

    /** Sử dụng `addMediaSources()` để update mediasources cho `mainExoPlayer` và `backgroundExoPlayer`
     *
     * cần phải call `prepare()` cho `mainExoPlayer` và `backgroundExoPlayer` sau khi addMediaSources.
     * */
    @OptIn(UnstableApi::class)
    fun loadMoreReels() {
        val newReels = mockMoreData1
        Timber.tag(tag).d("Load more reels, size: ${newReels.size}")
        val currentReelSize = _reels.value.size
        _reels.value += newReels
        fetchSizes.add(newReels.size)
        viewModelScope.launch {
            val preloadResult = preloadMediaSourceForRange(currentReelSize, _reels.value.size)
            if (!preloadResult.isSuccess) return@launch
            val (shortenSources, mergedSources) = createShortenSourcesAndMergeSources(newReels, baseIndex = currentReelSize)
            if (backgroundExoPlayer == null || mainExoPlayer == null) {
                Timber.tag(tag).w("Missing backgroundExoPlayer or mainExoPlayer")
                return@launch
            }
            if (shortenSources.isNotEmpty()) {
                backgroundExoPlayer!!.addMediaSources(shortenSources)
                backgroundExoPlayer!!.prepare()
            }
            if (mergedSources.isNotEmpty()) {
                mainExoPlayer!!.addMediaSources(mergedSources)
                mainExoPlayer!!.prepare()
            }
        }
    }

    /** check xem player đã set MediaSource và prepared chưa.
     *
     * cần có logic check này bởi vì lần đầu render ui video players thì page index 0 rendered nhưng mà processMainPlayer chưa dc call
     *
     * important: sử dụng seekTo(page, 0) -> 0: giảm thời gian render first frame
     *
     * important: chờ `seekTo()` hoàn tất -> update `player` of `playerView` and start `play`
     *
     * nếu ko chờ `seekTo()` hoàn thành và `onRenderedFirstFrame()` emitted mà play ngay thì sẽ bị nháy last frame của page trước đó do `mainExoPlayer` giữ frame cũ và đang processing `seekTo()` nhưng `playerView` lại render trước)
     * */
    @OptIn(UnstableApi::class)
    fun seekToPageAndPlayIfNeeded(page: Int, onCompleted: (ExoPlayer) -> Unit) {
        val seekToPageTag = "seekToPageAndPlayIfNeeded"
        _isAllowUserScrollEnabled.value = false
        mainExoPlayer?.let { player ->
            if (page != 0) {
                mainPlayerTracker.invalidateSession()
            }
            Timber.tag(seekToPageTag).d("seek to page $page and play if needed, mediaItemCount: ${player.mediaItemCount}, playbackState: ${player.playbackState}")
            if (player.mediaItemCount > 0 && player.playbackState != Player.STATE_IDLE) {
                // Kiểm tra nếu page đã seek và first frame đã render
                if (player.currentMediaItemIndex == page && _firstFrameRenderedPage.value == page) {
                    Timber.tag(seekToPageTag).d("Page $page already seeked and first frame rendered, attaching playerView")
                    if (!player.isPlaying) {
                        player.playWhenReady = true
                        onCompleted(player)
                    }
                    return
                }
                val listener = object : Player.Listener {
                    var localSeekCompleted = false
                    var localFirstFrameRendered = false
                    override fun onRenderedFirstFrame() {
                        if (currentSettledPage == page) {
                            Timber.tag(seekToPageTag).d("First frame rendered for page $page")
                            localFirstFrameRendered = true
                            tryAttachAndPlay()
                        }
                    }
                    override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
                        if (reason == Player.DISCONTINUITY_REASON_SEEK && newPosition.mediaItemIndex == page) {
                            Timber.tag(seekToPageTag).d("Seek to page $page completed")
                            localSeekCompleted = true
                            tryAttachAndPlay()
                        }
                    }
                    private fun tryAttachAndPlay() {
                        if (localSeekCompleted && localFirstFrameRendered) {
                            Timber.tag(seekToPageTag).d("Attaching playerView and playing for page $page")
                            _firstFrameRenderedPage.value = currentSettledPage
                            _isAllowUserScrollEnabled.value = true
                            if (!player.isPlaying) {
                                player.playWhenReady = true
                                onCompleted(player)
                            }
                            player.removeListener(this)
                        }
                    }
                }
                player.addListener(listener)
                player.seekTo(page, SEEK_TO_DEFAULT_VALUE)
            }
        }
    }

    /** cần check `isPlaying` Bởi vì `startPlay` có thể bị triggered spam từ `ReelVideoPlayer`
     * */
    fun startPlay(onCompleted: (ExoPlayer) -> Unit) {
        mainExoPlayer?.let { player ->
            if (!player.isPlaying) {
                player.playWhenReady = true
                onCompleted(player)
            }
        }
    }

    /** cần check `isPlaying` Bởi vì `startPause` có thể bị triggered spam từ `ReelVideoPlayer`
     * */
    @OptIn(UnstableApi::class)
    fun startPause(onCompleted: (ExoPlayer) -> Unit) {
        mainExoPlayer?.let { player ->
            if (player.isPlaying) {
                player.playWhenReady = false
                onCompleted(player)
            }
        }
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