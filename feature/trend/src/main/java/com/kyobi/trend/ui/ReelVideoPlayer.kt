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
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
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
    val createdMediaSources = remember { mutableStateMapOf<String, Boolean>() } // Theo dõi MediaSource đã tạo
    val coroutineScope = rememberCoroutineScope()
    val isActivePage by rememberUpdatedState(pageIndex == pagerState.currentPage)
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)

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

    fun createExoPlayer(mediaItem: MediaItem, playWhenReady: Boolean): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(true) // Bật chế độ fallback để thử codec khác nếu lỗi
            .forceDisableMediaCodecAsynchronousQueueing() // Tắt asynchronous queueing
        val player = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(5000, 10000, 1000, 5000)
                    .build()
            )
            .setReleaseTimeoutMs(5000)
            .build()
        startTimes[pageIndex] = System.currentTimeMillis()
        return player.apply {
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            repeatMode = Player.REPEAT_MODE_ALL
            setMediaSource(viewModel.startCreateMediaSource(mediaItem), 0L)
            this.playWhenReady = playWhenReady
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
            })
        }
    }

    // Quản lý ExoPlayer: pre-init, play, stop, dispose
    LaunchedEffect(pageIndex, pagerState) {
        snapshotFlow { pagerState.currentPage to pagerState.currentPageOffsetFraction }
            .distinctUntilChanged()
            .collect { (currentPage, offset) ->
                val isPageVisible = pageIndex == currentPage || (pageIndex == currentPage + 1 && offset < -0.1f) || (pageIndex == currentPage - 1 && offset > 0.1f)
                // Khởi tạo ban đầu hoặc khi page trở thành currentPage
                if (!players.containsKey(currentPage) && viewModel.reels.value.getOrNull(currentPage) != null) {
                    val mediaItem = MediaItem.fromUri(viewModel.reels.value[currentPage].videoUrl).buildUpon()
                        .setMediaId(viewModel.reels.value[currentPage].videoUrl).build()
                    players[currentPage] = createExoPlayer(mediaItem, playWhenReady = true)
                    Timber.tag(tag).d("Initialized ExoPlayer for current page $currentPage")
                }
                // Pre-init cho page tiếp theo và page trước
                if (isActivePage) {
                    // Pre-init next page
                    if (currentPage + 1 < viewModel.reels.value.size && !players.containsKey(currentPage + 1)) {
                        val nextUrl = viewModel.reels.value[currentPage + 1].videoUrl
                        if (!createdMediaSources.containsKey(nextUrl) || !players.containsKey(currentPage + 1)) {
                            val mediaItem = MediaItem.fromUri(nextUrl).buildUpon()
                                .setMediaId(nextUrl).build()
                            players[currentPage + 1] = createExoPlayer(mediaItem, playWhenReady = false)
                            startTimes[currentPage + 1] = System.currentTimeMillis()
                            createdMediaSources[nextUrl] = true
                            Timber.tag(tag).d("Pre-init ExoPlayer for next page ${currentPage + 1}")
                        }
                    }
                    // Ko cần Pre-init previous page
                }
                // Dừng video của previous page asap
                players.forEach { (index, player) ->
                    if (index == currentPage) {
                        if (!player.isPlaying) {
                            player.setPriority(C.PRIORITY_PLAYBACK)
                            player.play()
                        }
                        Timber.tag(tag).d("Playing ExoPlayer for page $currentPage")
                    } else {
                        player.setPriority(C.PRIORITY_PLAYBACK_PRELOAD)
                        player.pause()
                        player.seekTo(0)
                        Timber.tag(tag).d("Paused ExoPlayer for page $index")
                    }
                }
                // Dispose các ExoPlayer không cần thiết (cách currentPage 2 bước) với debounce
                val playersToDispose = players.keys.filter { it !in (currentPage - 2..currentPage + 2) }
                if (playersToDispose.isNotEmpty()) {
                    coroutineScope.launch(Dispatchers.Main) {
                        delay(500L)
                        playersToDispose.forEach { index ->
                            viewModel.reels.value.getOrNull(index)?.videoUrl?.let { url ->
                                players.remove(index)?.let { player ->
                                    try {
                                        if (player.isPlaying) {
                                            player.pause()
                                        }
                                        player.stop()
                                        Timber.tag(tag).d("Stopping ExoPlayer for page $index before release")
                                        player.release()
                                        createdMediaSources.remove(url)
                                        Timber.tag(tag).d("Disposed ExoPlayer for page $index")
                                    } catch (e: Exception) {
                                        Timber.tag(tag).e(e, "Failed to release ExoPlayer for page $index")
                                    }
                                }
                            }
                        }
                    }
                }
                // Gán PlayerView khi page visible
                if (isPageVisible && players.containsKey(pageIndex)) {
                    playerView.player = players[pageIndex]
                    Timber.tag(tag).d("PlayerView updated for page $pageIndex")
                } else if (!isPageVisible) {
                    playerView.player = null
                }
                // Load more
                if (currentPage % 3 == 0 && currentPage > 0 && currentPage < viewModel.reels.value.size - 3) {
                    onFetchMore()
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
                            Timber.tag(tag).d("Tapped page $pageIndex, playWhenReady=${it.playWhenReady}")
                        }
                    }
                )
            }
    )

    if (showThumbnail && reel.thumbnailUrl?.isNotEmpty() == true) {
        AsyncImage(
            model = reel.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize().zIndex(1f),
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
                    if (player.isPlaying) {
                        player.pause()
                    }
                    player.stop()
                    Timber.tag(tag).d("Stopping ExoPlayer for page $pageIndex before release")
                    player.release()
                    playerView.player = null
                    createdMediaSources.remove(reel.videoUrl)
                    Timber.tag(tag).d("Disposed ExoPlayer for page $pageIndex")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to release ExoPlayer for page $pageIndex")
                }
            }
        }
    }
}