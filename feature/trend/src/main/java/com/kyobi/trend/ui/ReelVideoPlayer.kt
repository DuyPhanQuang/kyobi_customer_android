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
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.VideoSize
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
    val fetchStates = remember { mutableStateMapOf<Int, Boolean>().apply { put(pageIndex, false) } }

    val playerView = remember(pageIndex) {
        PlayerView(context).apply {
            useController = false
            setKeepContentOnPlayerReset(true)
            setEnableComposeSurfaceSyncWorkaround(true)
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            keepScreenOn = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    // Lấy ExoPlayer từ ViewModel
    val player = viewModel.getPlayer()

    // Khởi tạo ExoPlayer và thiết lập MediaSource
    LaunchedEffect(
        pageIndex,
        pagerState,
        viewModel.reels.value,
    ) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                val isCurrentPage = pageIndex == settledPage
                if (isCurrentPage && viewModel.reels.value.getOrNull(settledPage) != null) {
                    viewModel.initializePlayer()
                    val reelData = viewModel.reels.value[settledPage]
                    Timber.tag(tag).d("Preparing to setup ExoPlayer for page $settledPage, reelData: $reelData")
                    val mediaItem = MediaItem.fromUri(reelData.shortenUrl).buildUpon()
                        .setMediaId(reelData.shortenUrl).build()
                    val fullMediaItem = MediaItem.fromUri(reelData.videoUrl).buildUpon()
                        .setMediaId(reelData.videoUrl).build()
                    viewModel.setupMediaSourceForPage(
                        shortenMediaItem = mediaItem,
                        fullMediaItem = fullMediaItem,
                        targetIndex = settledPage,
                        listener = object : Player.Listener {
                            override fun onRenderedFirstFrame() {
                                val startTime = startTimes[settledPage]
                                if (startTime != null) {
                                    val duration = System.currentTimeMillis() - startTime
                                    Timber.tag(tag).d("Time to render first frame for page $settledPage: $duration ms")
                                    startTimes.remove(settledPage)
                                }
                                Timber.tag(tag).d("First frame rendered for page $settledPage")
                                showThumbnail = false
                            }
                            override fun onPlayerError(error: PlaybackException) {
                                Timber.tag(tag).e(error, "Player error for page $settledPage")
                            }
                            override fun onVideoSizeChanged(videoSize: VideoSize) {
                                Timber.tag(tag).d("Video size changed for page $settledPage: ${videoSize.width}x${videoSize.height}")
                            }
                            override fun onPlaybackStateChanged(state: Int) {
                                Timber.tag(tag).d("Playback state changed for page $settledPage: $state")
                            }
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                                Timber.tag(tag).d("Media item transition to mediaId ${mediaItem?.mediaId} for page $settledPage")
                            }
                            override fun onPositionDiscontinuity(
                                oldPosition: Player.PositionInfo,
                                newPosition: Player.PositionInfo,
                                reason: Int
                            ) {
                                if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                                    Timber.tag(tag).d("Auto transition (likely from shorten to full) at page $settledPage")
                                }
                            }
                            override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
                                Timber.tag(tag).d("Audio attributes changed for page $settledPage: contentType=${audioAttributes.contentType}, usage=${audioAttributes.usage}, flags=${audioAttributes.flags}")
                            }
                            override fun onAudioSessionIdChanged(audioSessionId: Int) {
                                Timber.tag(tag).d("Audio session ID changed for page $settledPage: audioSessionId=$audioSessionId")
                            }
                            override fun onVolumeChanged(volume: Float) {
                                Timber.tag(tag).d("Volume changed for page $settledPage: volume=$volume")
                            }
                            override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
                                Timber.tag(tag).d("Device volume changed for page $settledPage: volume=$volume, muted=$muted")
                            }
                        }
                    )
                    playerView.player = viewModel.getPlayer()
                    startTimes[settledPage] = System.currentTimeMillis()
                    Timber.tag(tag).d("Initialized ExoPlayer for current page $settledPage")
                }
                if (isCurrentPage && player != null) {
                    viewModel.startPlay(settledPage)
                    Timber.tag(tag).d("Playing ExoPlayer for page $settledPage")
                }
                val hasFetchedForPage = fetchStates[pageIndex] ?: false
                if (settledPage >= viewModel.reels.value.size - 5 && !hasFetchedForPage && !viewModel.isFetching.value) {
                    onFetchMore()
                    fetchStates[pageIndex] = true
                    Timber.tag(tag).d("Triggered load more at page $settledPage")
                }
            }
    }

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

    DisposableEffect(lifecycleOwner) {
        Timber.tag(tag).d("lifecycle called")
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    viewModel.startPause(pageIndex)
                }
                Lifecycle.Event.ON_START -> {
                    if (pageIndex == pagerState.settledPage) {
                        viewModel.startPlay(pageIndex)
                    }
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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