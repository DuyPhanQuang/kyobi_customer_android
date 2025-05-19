package com.kyobi.trend.ui

import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import kotlinx.coroutines.flow.distinctUntilChanged
import timber.log.Timber
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

@OptIn(UnstableApi::class)
@Composable
fun ReelVideoPlayer(
    pagerState: PagerState,
    pageIndex: Int,
    viewModel: ReelPlaybackViewModel,
    imageLoader: ImageLoader,
    onSingleTap: (ExoPlayer) -> Unit,
) {
    val tag = "ReelVideoPlayer"
    val context = LocalContext.current
    var showThumbnail by remember(pageIndex) { mutableStateOf(true) }
    val lifecycleOwner by rememberUpdatedState(LocalLifecycleOwner.current)
    var isPaused by remember(pageIndex) { mutableStateOf(false) }
    // Lưu offset trước đó và ngưỡng max/min
    val offsetState = remember(pageIndex) { mutableStateOf(Triple(0f, 0f, 0f)) } // (current, max, min)

    val playerView = remember(pageIndex) {
        PlayerView(context).apply {
            useController = false
            setKeepContentOnPlayerReset(false)
            setEnableComposeSurfaceSyncWorkaround(true)
            keepScreenOn = true
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            setShutterBackgroundColor(Color.TRANSPARENT)
            setBackgroundColor(Color.TRANSPARENT)
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        }
    }

    val player = viewModel.getMainPlayer()

    val reelsData = viewModel.reels.value

    // hide showThumbnail
    LaunchedEffect(pageIndex, viewModel.firstFrameRendered) {
        viewModel.firstFrameRendered.collect { renderedPage ->
            if (renderedPage == pageIndex) {
                showThumbnail = false
                Timber.tag(tag).d("Hiding thumbnail for page $pageIndex")
            }
        }
    }

    /** main logic của snapped page
     * */
    LaunchedEffect(pageIndex, pagerState, viewModel.reels.value, player) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                val isCurrentPage = pageIndex == settledPage
                val reelItem = viewModel.reels.value.getOrNull(settledPage)
                if (isCurrentPage && reelItem != null && player != null) {
                    viewModel.updateSettledPage(settledPage, playerView)
                    viewModel.seekToPageAndPlayIfNeeded(settledPage, playerView)
                    isPaused = false // Reset trạng thái khi page snapped
                    offsetState.value = Triple(0f, 0f, 0f) // Reset offset khi snapped
                    Timber.tag(tag).d("ExoPlayer seek to page $settledPage then start playing if needed")
                }
            }
    }

    /** logic start/stop sắp thành current page/current page sắp cũ (forward/backward)
     * cover handle trường hợp user chơi chiêu scroll giữ từ từ ko thả tay
     * */
    LaunchedEffect(pageIndex, pagerState, player) {
        snapshotFlow { pagerState.currentPageOffsetFraction to pagerState.settledPage }
            .collect { (offset, settledPage) ->
                // Chỉ xử lý nếu offset thay đổi đáng kể
                val (prevOffset, maxOffset, minOffset) = offsetState.value
                if (abs(offset - prevOffset) < 0.01f && offset != 0f) return@collect // Bỏ qua thay đổi nhỏ
                // Cập nhật offset tối đa/min
                offsetState.value = Triple(offset, max(maxOffset, offset), min(minOffset, offset))
                Timber.tag(tag).d("Offset check: pageIndex=$pageIndex, settledPage=$settledPage, offset=$offset, maxOffset=$maxOffset, minOffset=$minOffset, isPlaying=${player?.isPlaying}, isPaused=$isPaused")
                // Reset isPaused khi offset rất nhỏ (video hiển thị gần 100%)
                if (pageIndex == settledPage && isPaused && abs(offset) < 0.05f) {
                    Timber.tag(tag).d("Reset isPaused and play continue - isPaused: pageIndex=$pageIndex, offset=$offset")
                    viewModel.startPlay(playerView)
                    isPaused = false
                    offsetState.value = Triple(offset, offset, offset) // Reset max/min
                }
                // Chỉ pause nếu page này là settledPage và video đang phát
                val shouldPause = pageIndex == settledPage && player != null && player.isPlaying && !isPaused
                if (shouldPause) {
                    // Pause khi next/back video hiển thị >= 35%
                    if (maxOffset >= 0.35f || minOffset <= -0.35f) {
                        Timber.tag(tag).d("Start Pause video at page $pageIndex, offset: $offset, maxOffset=$maxOffset, minOffset=$minOffset")
                        viewModel.startPause(playerView)
                        isPaused = true
                    }
                } else {
                    Timber.tag(tag).d("Skip pause: pageIndex=$pageIndex, settledPage=$settledPage, isPlaying=${player?.isPlaying}, isPaused=$isPaused")
                }
            }
    }

    DisposableEffect(lifecycleOwner) {
        Timber.tag(tag).d("lifecycle called")
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    viewModel.startPause(playerView)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { playerView },
            modifier = Modifier
                .fillMaxSize()
                .zIndex(0f)
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
        if (showThumbnail) {
            AppImage(
                modifier = Modifier
                    .fillMaxSize()
                    .zIndex(1f),
                imageUrl = reelsData[pageIndex].thumbnailUrl,
                contentDescription = "Reel thumbnail image ${reelsData[pageIndex].thumbnailUrl}",
                contentScale = ContentScale.Crop,
                isSkeletonEnabled = false,
                imageLoader = imageLoader
            )
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (pageIndex != pagerState.settledPage) {
                playerView.player = null
                Timber.tag(tag).d("Detached PlayerView for page $pageIndex on dispose")
            }
        }
    }
}