package com.kyobi.trend.ui

import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.upstream.DefaultLoadErrorHandlingPolicy
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@OptIn(UnstableApi::class)
@Composable
fun VideoPlayer(
    reel: Reel,
    pagerState: PagerState,
    pageIndex: Int,
    viewModel: ReelPlaybackViewModel,
    onSingleTap: (ExoPlayer) -> Unit,
    onFetchMore: () -> Unit
) {
    val tag = "ReelVideoPlayer"
    val context = LocalContext.current
    var showThumbnail by remember(pageIndex) { mutableStateOf(true) }
    val players = remember { mutableStateMapOf<Int, ExoPlayer>() }
    val startTimes = remember { mutableStateMapOf<Int, Long>() }
    val createdMediaSources = remember { mutableStateMapOf<String, MediaSource?>() } // Theo dõi MediaSource đã tạo
    val coroutineScope = rememberCoroutineScope()
    val isActivePage by rememberUpdatedState(pageIndex == pagerState.currentPage)
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
    // Sử dụng mutableStateMapOf để quản lý hasFetchedForPage theo pageIndex
    val fetchStates = remember { mutableStateMapOf<Int, Boolean>().apply { put(pageIndex, false) } }

    // Tạo và giữ PlayerView bằng remember
    val playerView = remember(pageIndex) {
        PlayerView(context).apply {
            useController = false
            setKeepContentOnPlayerReset(true)
            setEnableComposeSurfaceSyncWorkaround(true)
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
            keepScreenOn = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    fun createExoPlayer(
        shortenMediaItem: MediaItem,
        fullMediaItem: MediaItem,
        playWhenReady: Boolean,
    ): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true) // Bật chế độ fallback để thử codec khác nếu lỗi
            .forceDisableMediaCodecAsynchronousQueueing() // Tắt asynchronous queueing
        val cacheDataSourceFactory = viewModel.mediaCache.getMediaSourceFactory()
        val player = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        15000,
                        45000,
                        2000,
                        5000)
                    .setTargetBufferBytes(-1)
                    .build()
            )
            .setReleaseTimeoutMs(5000L)
            .setMediaSourceFactory(cacheDataSourceFactory)
            .build()
        startTimes[pageIndex] = System.currentTimeMillis()
        // Tạo ConcatenatingMediaSource2
        val shortenMediaSource = viewModel.startCreateMediaSource(shortenMediaItem)
        val fullMediaSource = viewModel.startCreateMediaSource(fullMediaItem)
        val newMediaSource: MediaSource = try {
            ConcatenatingMediaSource2.Builder()
                .add(shortenMediaSource, 10_000L)
                .add(fullMediaSource, 180_000L)
                .build().also {
                    Timber.tag(tag).d("ConcatenatingMediaSource2 created for page $pageIndex")
                }
        } catch(e: Exception) {
            Timber.tag(tag).e(e, "Failed to create ConcatenatingMediaSource2 for page $pageIndex")
            throw e
        }
        return player.apply {
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            repeatMode = Player.REPEAT_MODE_OFF
            setMediaSource(newMediaSource, false)
            volume = 0f
            this.playWhenReady = false
            prepare()
            addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    val startTime = startTimes[pageIndex]
                    if (startTime != null) {
                        val duration = System.currentTimeMillis() - startTime
                        Timber.tag(tag).d("Time to render first frame for page $pageIndex: $duration ms")
                        startTimes.remove(pageIndex)
                    }
                    Timber.tag(tag).d("First frame rendered for page $pageIndex")
                    if (pageIndex == pagerState.currentPage) showThumbnail = false
                }
                override fun onPlayerError(error: PlaybackException) {
                    Timber.tag(tag).e(error, "Player error for page $pageIndex")
                }
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    Timber.tag(tag).d("Video size changed for page $pageIndex: ${videoSize.width}x${videoSize.height}")
                }
                override fun onPlaybackStateChanged(state: Int) {
                    Timber.tag(tag).d("Playback state changed for page $pageIndex: $state")
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    Timber.tag(tag).d("Media item transition to mediaId ${mediaItem?.mediaId} for page $pageIndex")
                }
                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                        Timber.tag(tag).d("Auto transition (likely from shorten to full) at page $pageIndex")
                    }
                }
            })
        }
    }

    // Quản lý ExoPlayer: pre-init, play, stop, dispose
    LaunchedEffect(
        pageIndex,
        pagerState,
        viewModel.reels.value,
    ) {
        snapshotFlow { pagerState.currentPage to pagerState.currentPageOffsetFraction }
            .distinctUntilChanged()
            .collect { (currentPage, offset) ->
                val isPageVisible = pageIndex == currentPage || (pageIndex == currentPage + 1 && offset < -0.1f) || (pageIndex == currentPage - 1 && offset > 0.1f)
                // Khởi tạo ban đầu hoặc khi page trở thành currentPage với ConcatenatingMediaSource2
                if (!players.containsKey(currentPage) && viewModel.reels.value.getOrNull(currentPage) != null) {
                    val reelData = viewModel.reels.value[currentPage]
                    Timber.tag(tag).d("Preparing to initialize ExoPlayer for page $currentPage, reelData: $reelData")
                    val mediaItem = MediaItem.fromUri(reelData.shortenUrl).buildUpon()
                        .setMediaId(reelData.shortenUrl).build()
                    val fullMediaItem = MediaItem.fromUri(reelData.videoUrl).buildUpon()
                        .setMediaId(reelData.videoUrl).build()
                    players[currentPage] = createExoPlayer(
                        shortenMediaItem = mediaItem,
                        fullMediaItem = fullMediaItem,
                        playWhenReady = true,
                    )
                    Timber.tag(tag).d("Initialized ExoPlayer for current page $currentPage")
                }
                // Pre-init cho page tiếp theo
                if (isActivePage && currentPage + 1 < viewModel.reels.value.size && !players.containsKey(currentPage + 1)) {
                    val reelData = viewModel.reels.value[currentPage + 1]
                    val mediaItem = MediaItem.fromUri(reelData.shortenUrl).buildUpon()
                        .setMediaId(reelData.shortenUrl).build()
                    val fullMediaItem = MediaItem.fromUri(reelData.videoUrl).buildUpon()
                        .setMediaId(reelData.videoUrl).build()
                    players[currentPage + 1] = createExoPlayer(
                        shortenMediaItem = mediaItem,
                        fullMediaItem = fullMediaItem,
                        playWhenReady = false,
                    )
                    startTimes[currentPage + 1] = System.currentTimeMillis()
                    Timber.tag(tag).d("Pre-init ExoPlayer for next page ${currentPage + 1}")
                }
                // Reset PlayerView trước khi cập nhật
                if (!isPageVisible) {
                    playerView.player = null
                }
                // Quản lý play/pause dựa trên isPageVisible
                players.forEach { (index, player) ->
                    if (isPageVisible && index == currentPage) {
                        player.setPriority(C.PRIORITY_PLAYBACK)
                        player.volume = 1f
                        player.repeatMode = Player.REPEAT_MODE_ALL
                        player.playWhenReady = true
                        player.play()
                        Timber.tag(tag).d("Playing ExoPlayer for page $currentPage")
                    } else {
                        player.setPriority(C.PRIORITY_PLAYBACK_PRELOAD)
                        player.volume = 0f
                        player.repeatMode = Player.REPEAT_MODE_OFF
                        player.playWhenReady = false
                        player.pause()
                        player.seekTo(0)
                        Timber.tag(tag).d("Paused ExoPlayer for page $index")
                    }
                }
                // Gán hoặc reset PlayerView
                if (isPageVisible && players.containsKey(pageIndex)) {
                    playerView.player = players[pageIndex]
                    Timber.tag(tag).d("PlayerView updated for page $pageIndex")
                } else {
                    playerView.player = null
                }
                // Dispose các ExoPlayer không cần thiết (cách currentPage 1 bước) với debounce
                val playersToDispose = players.keys.filter { it !in (currentPage - 1..currentPage + 1) && it != pageIndex }
                if (playersToDispose.isNotEmpty()) {
                    coroutineScope.launch(Dispatchers.Main) {
                        delay(1000L)
                        playersToDispose.forEach { index ->
                            viewModel.reels.value.getOrNull(index)?.let { reelData ->
                                players.remove(index)?.let { player ->
                                    try {
                                        player.volume = 0f
                                        player.playWhenReady = false
                                        player.repeatMode = Player.REPEAT_MODE_OFF
                                        player.pause()
                                        player.stop()
                                        player.clearMediaItems()
                                        Timber.tag(tag).d("Stopping ExoPlayer for page $index before release")
                                        player.release()
                                        createdMediaSources.remove(reelData.shortenUrl)
                                        createdMediaSources.remove(reelData.videoUrl)
                                        Timber.tag(tag).d("Disposed ExoPlayer for page $index")
                                    } catch (e: Exception) {
                                        Timber.tag(tag).e(e, "Failed to release ExoPlayer for page $index")
                                    }
                                }
                            }
                        }
                    }
                }
                // Trigger load more
                val hasFetchedForPage = fetchStates[pageIndex] ?: false
                if (currentPage >= viewModel.reels.value.size - 5 && !hasFetchedForPage && !viewModel.isFetching.value) {
                    onFetchMore()
                    fetchStates[pageIndex] = true
                    Timber.tag(tag).d("Triggered load more at page $currentPage")
                }
            }
    }

    AndroidView(
        factory = { playerView },
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        players[pageIndex]?.let {
                            onSingleTap(it)
                            Timber.tag(tag)
                                .d("Tapped page $pageIndex, playWhenReady=${it.playWhenReady}")
                        }
                    }
                )
            }
    )

    if (showThumbnail && reel.thumbnailUrl?.isNotEmpty() == true) {
        AsyncImage(
            model = reel.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            contentScale = ContentScale.Crop
        )
    }

    // Quản lý lifecycle
    DisposableEffect(lifecycleOwner, isActivePage) {
        Timber.tag(tag).d("lifecycle called")
        val observer = LifecycleEventObserver { _, event ->
            if (isActivePage && players[pageIndex] != null) {
                when (event) {
                    Lifecycle.Event.ON_STOP -> players[pageIndex]?.pause()
                    Lifecycle.Event.ON_START -> players[pageIndex]?.play()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Release tất cả ExoPlayer khi composable bị dispose
    DisposableEffect(pageIndex) {
        onDispose {
            players.remove(pageIndex)?.let { player ->
                try {
                    player.volume = 0f
                    player.repeatMode = Player.REPEAT_MODE_OFF
                    player.playWhenReady = false
                    player.pause()
                    player.stop()
                    player.clearMediaItems()
                    Timber.tag(tag).d("Stopping ExoPlayer for page $pageIndex before release")
                    player.release()
                    playerView.player = null
                    createdMediaSources.remove(reel.shortenUrl)
                    createdMediaSources.remove(reel.videoUrl)
                    Timber.tag(tag).d("Disposed ExoPlayer for page $pageIndex")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to release ExoPlayer for page $pageIndex")
                }
            }
        }
    }
}