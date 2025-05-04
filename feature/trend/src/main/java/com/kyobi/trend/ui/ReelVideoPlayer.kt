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
    onSingleTap: (ExoPlayer) -> Unit
) {
    val tag = "ReelVideoPlayer"
    val context = LocalContext.current
    var showThumbnail by remember(pageIndex) { mutableStateOf(true) }
    var exoPlayer by remember(pageIndex) { mutableStateOf<ExoPlayer?>(null) }
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

    // Quản lý tạo/release ExoPlayer
    LaunchedEffect(pageIndex, pagerState) {
        snapshotFlow { pagerState.currentPage to pagerState.currentPageOffsetFraction }
            .distinctUntilChanged()
            .collect { (currentPage, offset) ->
                val isPageVisible = pageIndex == currentPage || (pageIndex == currentPage + 1 && offset < -0.1f) || (pageIndex == currentPage - 1 && offset > 0.1f)
                if (isPageVisible && exoPlayer == null) {
                    exoPlayer = ExoPlayer.Builder(context)
                        .setLoadControl(
                            DefaultLoadControl.Builder()
                                .setBufferDurationsMs(5000, 10000, 1000, 5000)
                                .build()
                        )
                        .build()
                        .apply {
                            val mediaItem = MediaItem.fromUri(reel.videoUrl).buildUpon()
                                .setMediaId(reel.videoUrl).build()
                            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
                            repeatMode = Player.REPEAT_MODE_ALL
                            setMediaSource(viewModel.startCreateMediaSource(mediaItem), 0L)
                            playWhenReady = pageIndex == currentPage
                            prepare()
                            addListener(object : Player.Listener {
                                override fun onRenderedFirstFrame() {
                                    Timber.tag(tag).d("First frame rendered for page $pageIndex")
                                    showThumbnail = false
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
                    playerView.player = exoPlayer
                    Timber.tag(tag).d("ExoPlayer created for page $pageIndex")
                } else if (!isPageVisible && exoPlayer != null) {
                    // Trì hoãn release để tránh bất đồng bộ
                    coroutineScope.launch(Dispatchers.Main) {
                        delay(300) // Delay 300ms để đảm bảo trang thực sự không còn hiển thị
                        if (pageIndex != pagerState.currentPage && exoPlayer != null) {
                            exoPlayer?.let { player ->
                                player.stop()
                                player.release()
                                exoPlayer = null
                                showThumbnail = true
                                playerView.player = null
                                Timber.tag(tag).d("ExoPlayer released for page $pageIndex")
                            }
                        }
                    }
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
                        exoPlayer?.let {
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
        val observer = LifecycleEventObserver { _, event ->
            if (isActivePage && exoPlayer != null) {
                when (event) {
                    Lifecycle.Event.ON_STOP -> exoPlayer?.pause()
                    Lifecycle.Event.ON_START -> exoPlayer?.play()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Release ExoPlayer khi composable bị dispose
    DisposableEffect(pageIndex) {
        onDispose {
            exoPlayer?.let { player ->
                player.stop()
                player.release()
                exoPlayer = null
                showThumbnail = true
                playerView.player = null
                Timber.tag(tag).d("ExoPlayer disposed for page $pageIndex")
            }
        }
    }
}