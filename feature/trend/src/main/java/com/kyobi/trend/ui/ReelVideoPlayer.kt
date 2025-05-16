package com.kyobi.trend.ui

import android.graphics.Color
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import coil.compose.AsyncImage
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.flow.distinctUntilChanged
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
    val startTimes = remember { mutableStateMapOf<Int, Long>() }
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)

    val playerView = remember(pageIndex) {
        PlayerView(context).apply {
            useController = false
            setKeepContentOnPlayerReset(true)
            setEnableComposeSurfaceSyncWorkaround(true)
            keepScreenOn = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            setShutterBackgroundColor(Color.TRANSPARENT)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // Lấy ExoPlayer từ ViewModel
    val player = viewModel.getPlayer()

    // Collect firstFrameRendered để update showThumbnail và đo time render first frame
    LaunchedEffect(pageIndex, viewModel.firstFrameRendered) {
        viewModel.firstFrameRendered.collect { renderedPage ->
            if (renderedPage == pageIndex) {
                showThumbnail = false
                val startTime = startTimes[pageIndex]
                if (startTime != null) {
                    val duration = System.currentTimeMillis() - startTime
                    Timber.tag(tag).d("Time to render first frame for page $pageIndex: $duration ms")
                    startTimes.remove(pageIndex)
                }
                Timber.tag(tag).d("Hiding thumbnail for page $pageIndex")
            }
        }
    }

    // Thiết lập ExoPlayer khi page hiện tại
    LaunchedEffect(
        pageIndex,
        pagerState,
        viewModel.reels.value,
        player,
    ) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                val isCurrentPage = pageIndex == settledPage
                if (isCurrentPage && viewModel.reels.value.getOrNull(settledPage) != null) {
                    val reelData = viewModel.reels.value[settledPage]
                    Timber.tag(tag).d("Preparing ExoPlayer for page $settledPage, reelData: $reelData")
                    viewModel.updateSettledPage(settledPage, playerView)
                    showThumbnail = true
                    startTimes[settledPage] = System.currentTimeMillis()
                    Timber.tag(tag).d("Initialized ExoPlayer for current page $settledPage")
                }
                if (isCurrentPage && player != null) {
                    viewModel.startPlay(settledPage, playerView)
                    Timber.tag(tag).d("Playing ExoPlayer for page $settledPage")
                }
            }
    }

    DisposableEffect(lifecycleOwner) {
        Timber.tag(tag).d("lifecycle called")
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    viewModel.startPause(pageIndex, playerView)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    if (showThumbnail && reel.thumbnailUrl?.isNotEmpty() == true) {
        AsyncImage(
            model = reel.thumbnailUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .zIndex(1f),
            contentScale = ContentScale.Crop,
        )
    }

    AndroidView(
        factory = { playerView },
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = {
                        player?.let {
                            it.playWhenReady = !it.isPlaying
                            Timber.tag(tag)
                                .d("Tapped page $pageIndex, playWhenReady=${it.playWhenReady}")
                        }
                    }
                )
            }
    )

    DisposableEffect(Unit) {
        onDispose {
            if (pageIndex != pagerState.settledPage) {
                playerView.player = null
                Timber.tag(tag).d("Detached PlayerView for page $pageIndex on dispose")
            }
        }
    }
}