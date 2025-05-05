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
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.exoplayer.source.MediaSource
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
    val createdMediaSources = remember { mutableStateMapOf<String, MediaSource?>() } // Theo dõi MediaSource đã tạo
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
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            keepScreenOn = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
    }

    var player by remember(pageIndex) { mutableStateOf<ExoPlayer?>(null) }

    fun createExoPlayer(
        shortenMediaItem: MediaItem,
        fullMediaItem: MediaItem,
        targetIndex: Int,
    ): ExoPlayer {
        val renderersFactory = DefaultRenderersFactory(context)
            .setEnableDecoderFallback(false)
            .forceDisableMediaCodecAsynchronousQueueing()
        val cacheDataSourceFactory = viewModel.mediaCache.getMediaSourceFactory(shouldCache = true)
        val createPlayer = ExoPlayer.Builder(context)
            .setRenderersFactory(renderersFactory)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(
                        4000,
                        12000,
                        1000,
                        2000)
                    .setTargetBufferBytes(-1)
                    .build()
            )
            .setMediaSourceFactory(cacheDataSourceFactory)
            .setReleaseTimeoutMs(1000L)
            .build()
        startTimes[targetIndex] = System.currentTimeMillis()
        val shortenMediaSource = viewModel.startCreateMediaSource(shortenMediaItem, shouldCache = true)
        val fullMediaSource = viewModel.startCreateMediaSource(fullMediaItem, shouldCache = false)
        val newMediaSource: MediaSource = try {
            ConcatenatingMediaSource2.Builder()
                .add(shortenMediaSource, 10_000L)
                .add(fullMediaSource, 180_000L)
                .build().also {
                    Timber.tag(tag).d("ConcatenatingMediaSource2 created for page $targetIndex")
                }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to create ConcatenatingMediaSource2 for page $targetIndex")
            throw e
        }
        return createPlayer.apply {
            setMediaSource(newMediaSource, false)
            videoScalingMode = C.VIDEO_SCALING_MODE_SCALE_TO_FIT
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 1f
            this.playWhenReady = true
            prepare()
            addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    val startTime = startTimes[targetIndex]
                    if (startTime != null) {
                        val duration = System.currentTimeMillis() - startTime
                        Timber.tag(tag).d("Time to render first frame for page $targetIndex: $duration ms")
                        startTimes.remove(targetIndex)
                    }
                    Timber.tag(tag).d("First frame rendered for page $targetIndex")
                    showThumbnail = false
                }
                override fun onPlayerError(error: PlaybackException) {
                    Timber.tag(tag).e(error, "Player error for page $targetIndex")
                }
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    Timber.tag(tag).d("Video size changed for page $targetIndex: ${videoSize.width}x${videoSize.height}")
                }
                override fun onPlaybackStateChanged(state: Int) {
                    Timber.tag(tag).d("Playback state changed for page $targetIndex: $state")
                }
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    Timber.tag(tag).d("Media item transition to mediaId ${mediaItem?.mediaId} for page $targetIndex")
                }
                override fun onPositionDiscontinuity(
                    oldPosition: Player.PositionInfo,
                    newPosition: Player.PositionInfo,
                    reason: Int
                ) {
                    if (reason == Player.DISCONTINUITY_REASON_AUTO_TRANSITION) {
                        Timber.tag(tag).d("Auto transition (likely from shorten to full) at page $targetIndex")
                    }
                }
                override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
                    super.onAudioAttributesChanged(audioAttributes)
                    Timber.tag(tag).d("Audio attributes changed for page $targetIndex: contentType=${audioAttributes.contentType}, usage=${audioAttributes.usage}, flags=${audioAttributes.flags}")
                }
                override fun onAudioSessionIdChanged(audioSessionId: Int) {
                    super.onAudioSessionIdChanged(audioSessionId)
                    Timber.tag(tag).d("Audio session ID changed for page $targetIndex: audioSessionId=$audioSessionId")
                }
                override fun onVolumeChanged(volume: Float) {
                    Timber.tag(tag).d("Volume changed for page $targetIndex: volume=$volume")
                }
                override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
                    Timber.tag(tag).d("Device volume changed for page $targetIndex: volume=$volume, muted=$muted")
                }
            })
        }
    }

    // Quản lý ExoPlayer: pre-init, play, pause
    LaunchedEffect(
        pageIndex,
        pagerState,
        viewModel.reels.value,
    ) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { settledPage ->
                val isCurrentPage = pageIndex == settledPage
                // Khởi tạo ExoPlayer cho page hiện tại nếu chưa có
                if (isCurrentPage && player == null && viewModel.reels.value.getOrNull(settledPage) != null) {
                    val reelData = viewModel.reels.value[settledPage]
                    Timber.tag(tag).d("Preparing to initialize ExoPlayer for page $settledPage, reelData: $reelData")
                    val mediaItem = MediaItem.fromUri(reelData.shortenUrl).buildUpon()
                        .setMediaId(reelData.shortenUrl).build()
                    val fullMediaItem = MediaItem.fromUri(reelData.videoUrl).buildUpon()
                        .setMediaId(reelData.videoUrl).build()
                    player = createExoPlayer(
                        shortenMediaItem = mediaItem,
                        fullMediaItem = fullMediaItem,
                        targetIndex = settledPage
                    )
                    playerView.player = player
                    Timber.tag(tag).d("Initialized ExoPlayer for current page $settledPage")
                }
                // Quản lý play/pause
                if (isCurrentPage && player != null) {
                    viewModel.startPlay(player, settledPage)
                    Timber.tag(tag).d("Playing ExoPlayer for page $settledPage")
                } else if (player != null) {
                    viewModel.startPause(player, pageIndex)
                    Timber.tag(tag).d("Paused ExoPlayer for page $pageIndex")
                }
                // Trigger load more
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

    // Quản lý lifecycle
    DisposableEffect(lifecycleOwner) {
        Timber.tag(tag).d("lifecycle called")
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP -> {
                    player?.let { viewModel.startPause(it, pageIndex) }
                }
                Lifecycle.Event.ON_START -> {
                    if (pageIndex == pagerState.settledPage) {
                        player?.let { viewModel.startPlay(it, pageIndex) }
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
            player?.let { playerToRelease ->
                try {
                    viewModel.startRelease(playerToRelease, pageIndex)
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