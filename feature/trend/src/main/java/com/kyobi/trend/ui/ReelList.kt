package com.kyobi.trend.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.viewpager2.widget.ViewPager2
import com.kyobi.trend.model.Reel
import com.kyobi.theme.kyobiTheme
import timber.log.Timber

@OptIn(UnstableApi::class)
@Composable
fun ReelList(
    reels: List<Reel>,
    topSystemBarHeight: Dp = Dp(0f),
    bottomNavBarHeight: Dp = Dp(0f)
) {
    val tag = "ReelList"
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val playbackViewModel: ReelPlaybackViewModel = hiltViewModel()
    val currentReels by rememberUpdatedState(reels)
    val adapter = remember { mutableStateOf<ReelAdapter?>(null) }
    val offscreenPageNumber = 5
    val preloadedMediaItems = remember { mutableMapOf<Int, MediaItem>() }
    val preloadedMediaSources = remember { mutableMapOf<Int, MediaSource>() }
    val pool = remember { mutableStateOf<PlayerPool?>(null) }
    val lastPos = remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        playbackViewModel.setReels(currentReels)
        preloadedMediaItems.clear()
        preloadedMediaSources.clear()
        currentReels.forEachIndexed { index, reel ->
            val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                .setMediaId(reel.videoUrl).build()
            preloadedMediaItems[index] = mediaItem
//            Timber.tag(tag).d("Preloaded mediaItem for position $index mediaItem: $mediaItem")
            val mediaSource = playbackViewModel.startCreateMediaSource(mediaItem)
            if (mediaSource != null) {
                preloadedMediaSources[index] = mediaSource
//                Timber.tag(tag).d("Preloaded mediaSource for position $index mediaSource: $mediaSource")
            }
            // 2) init player pool
            if (preloadedMediaSources.size == reels.size) {
                pool.value = playbackViewModel.initPlayerPool(preloadedMediaItems, preloadedMediaSources)
                pool.value?.let { p ->
                    Timber.tag(tag).d("===> Pool initialized: $p")
                    Timber.tag(tag).d("attach initial 2 view")
                    adapter.value!!.attachPlayerViewAt(0, p.currentPlayerView)
                    preloadedMediaSources[0]?.let { mediaSource ->
                        p.currentPlayer.apply {
                            Timber.tag(tag).d("Set mediaSource and prepare then play current player at first time")
                            setMediaSource(mediaSource, true)
                            playWhenReady = true
                            prepare()
                            repeatMode = Player.REPEAT_MODE_ONE
                            volume = 1f
                        }
                        if (preloadedMediaSources.size > 1) {
                            adapter.value!!.attachPlayerViewAt(1, p.nextPlayerView)
                            Timber.tag(tag).d("Set mediaSource but not prepare not play next player at time first")
                            p.nextPlayer.apply {
                                prepareForNext(preloadedMediaSources[1])
                            }
                            Timber.tag(tag).d("Check nextPlayer instance: ${p.nextPlayer}")
                        }
                    }
                }
            }
        }
    }

    AndroidView(
        modifier = Modifier
            .background(MaterialTheme.kyobiTheme.colors.primary)
            .fillMaxSize()
            .padding(top = 0.dp, bottom = bottomNavBarHeight),
        factory = { context2 ->
            Timber.tag(tag).d("===> Creating ViewPager2")
            ViewPager2(context2).apply {
                orientation = ViewPager2.ORIENTATION_VERTICAL
                adapter.value = ReelAdapter(
                    reels = currentReels,
                    context = context2,
                    lifecycleOwner = lifecycleOwner,
                    viewPager = this,
                    playbackViewModel = playbackViewModel)
                this.adapter = adapter.value
                offscreenPageLimit = offscreenPageNumber // Giới hạn preload items trước/sau
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        val pp = pool.value ?: run {
                            Timber.tag(tag).d("===> Pool is null, skipping")
                            return
                        }
                        playbackViewModel.onPageSelected(position)
                        Timber.tag(tag).d("===> onPageSelected($position), lastPos = ${lastPos.intValue}")
                        // Xác định hướng scroll
                        val forward = position > lastPos.intValue
                        // Hoán đổi trong pool cho player và playerView
                        if (forward) {
                            val oldPrev = pp.prevPlayer
                            pp.prevPlayer = pp.currentPlayer
                            pp.currentPlayer = pp.nextPlayer
                            pp.nextPlayer = oldPrev
                            val oldPrevView = pp.prevPlayerView
                            pp.prevPlayerView = pp.currentPlayerView
                            pp.currentPlayerView = pp.nextPlayerView
                            pp.nextPlayerView = oldPrevView
                        } else {
                            val oldNext = pp.nextPlayer
                            pp.nextPlayer = pp.currentPlayer
                            pp.currentPlayer = pp.prevPlayer
                            pp.prevPlayer = oldNext
                            val oldNextView = pp.nextPlayerView
                            pp.nextPlayerView = pp.currentPlayerView
                            pp.currentPlayerView = pp.prevPlayerView
                            pp.prevPlayerView = oldNextView
                        }
                        // Log trạng thái sau khi swap
                        Timber.tag(tag).d("After swap: currentPlayer instance=${pp.currentPlayer}, nextPlayer instance=${pp.nextPlayer}, prevPlayer instance=${pp.prevPlayer}")
                        // Hoán đổi trong adapter
                        adapter.value!!.attachPlayerViews(position, pp)
                        pp.currentPlayer.apply {
                            // Đảm bảo media source đã được set từ trước (preload)
                            // chỉ nên thực hiện hàm này ví lý do nào đó lần gần nhất nextPlayer/prevPlayer prepare thất bại
                            if (forward) {
                                Timber.tag(tag).d("case forward")
                            } else {
                                seekTo(0)
                                Timber.tag(tag).d("case backward")
                            }
                            playWhenReady = true
                            repeatMode = Player.REPEAT_MODE_ONE
                            volume = 1f
                        }
                        pp.prevPlayer.apply { pauseForPrev() }
                        pp.nextPlayer.apply { prepareForNext(preloadedMediaSources[position + 1]) }
                        lastPos.intValue = position
                    }
                })
            }
        }
    )
}

@UnstableApi
fun ExoPlayer.prepareForNext(source: MediaSource?) {
    try {
        pause()
        stop()
        clearMediaItems()
        source?.let {
            setMediaSource(it, true)
            prepare()
            repeatMode = Player.REPEAT_MODE_OFF
            volume = 0f
            Timber.tag("ExoPlayer").d("prepareForNext done")
        }
        playWhenReady = false
        seekTo(0)
    } catch (e: Exception) {
        Timber.tag("ExoPlayer").e(e, "Error preparing next player")
    }
}

@UnstableApi
fun ExoPlayer.pauseForPrev() {
    try {
        pause()
        playWhenReady = false
        repeatMode = Player.REPEAT_MODE_OFF
        volume = 0f
        Timber.tag("ExoPlayer").d("pauseForPrev done")
    } catch (e: Exception) {
        Timber.tag("ExoPlayer").e(e, "Error pausing prev player")
    }
}