package com.kyobi.trend.ui

import android.view.SurfaceView
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.source.MediaSource
import androidx.viewpager2.widget.ViewPager2
import com.kyobi.trend.model.Reel
import com.kyobi.theme.kyobiTheme
import com.kyobi.trend.extension.pauseForPrev
import com.kyobi.trend.extension.playForCurrent
import com.kyobi.trend.extension.prepareForNext
import com.kyobi.trend.extension.resetNextBeforeReuse
import com.kyobi.trend.extension.resetPrevBeforeReuse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    val offscreenPageNumber = 10
    val preloadedMediaItems = remember { mutableMapOf<Int, MediaItem>() }
    val preloadedMediaSources = remember { mutableStateMapOf<Int, MediaSource>() }
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
            val mediaSource = playbackViewModel.startCreateMediaSource(mediaItem)
            if (mediaSource != null) {
                preloadedMediaSources[index] = mediaSource
            }
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
                        playForCurrent(isFirstTime = true, forward = false, source = mediaSource)
                    }
                    if (preloadedMediaSources.size > 1) {
                        adapter.value!!.attachPlayerViewAt(1, p.nextPlayerView)
                        p.nextPlayer.apply {
                            prepareForNext(preloadedMediaSources[1])
                        }
                        Timber.tag(tag).d("Check nextPlayer instance: ${p.nextPlayer}")
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
                        val mediaSources = playbackViewModel.getMediaSources()
                        if (mediaSources.isEmpty()) {
                            Timber.tag(tag).d("===> mediaSources is empty, skipping")
                            return
                        }
                        playbackViewModel.onPageSelected(position)
                        Timber.tag(tag).d("===> onPageSelected($position), lastPos = ${lastPos.intValue}")
                        // Xác định hướng scroll
                        val forward = position > lastPos.intValue
                        // Hoán đổi trong pool cho player và playerView/playerView.player và surfaceHolders
                        if (forward) {
                            // Clear prevPlayer và prevPlayerView trước khi tái sử dụng
                            pp.prevPlayer.apply {
                                resetPrevBeforeReuse(pp)
                            }
                            pp.prevPlayerView.let { view ->
                                view.player = null
                                Timber.tag(tag).d("Cleared prevPlayerView")
                            }
                            // Swap Player
                            val oldPrev = pp.prevPlayer
                            pp.prevPlayer = pp.currentPlayer
                            pp.currentPlayer = pp.nextPlayer
                            pp.nextPlayer = oldPrev
                            // Swap PlayerView
                            val oldPrevView = pp.prevPlayerView
                            pp.prevPlayerView = pp.currentPlayerView
                            pp.currentPlayerView = pp.nextPlayerView
                            pp.nextPlayerView = oldPrevView
                            // Cập nhật player cho PlayerView
                            pp.prevPlayerView.player = pp.prevPlayer
                            pp.currentPlayerView.player = pp.currentPlayer
                            pp.nextPlayerView.player = pp.nextPlayer
                        } else {
                            // Clear nextPlayer và nextPlayerView trước khi tái sử dụng
                            pp.nextPlayer.apply {
                                resetNextBeforeReuse(pp)
                            }
                            pp.nextPlayerView.let { view ->
                                view.player = null
                                Timber.tag(tag).d("Cleared nextPlayerView")
                            }
                            // Swap Player
                            val oldNext = pp.nextPlayer
                            pp.nextPlayer = pp.currentPlayer
                            pp.currentPlayer = pp.prevPlayer
                            pp.prevPlayer = oldNext
                            // Swap PlayerView
                            val oldNextView = pp.nextPlayerView
                            pp.nextPlayerView = pp.currentPlayerView
                            pp.currentPlayerView = pp.prevPlayerView
                            pp.prevPlayerView = oldNextView
                            // Cập nhật player cho PlayerView
                            pp.nextPlayerView.player = pp.nextPlayer
                            pp.currentPlayerView.player = pp.currentPlayer
                            pp.prevPlayerView.player = pp.prevPlayer
                        }
                        Timber.tag(tag).d("After swap: currentPlayer instance=${pp.currentPlayer}, nextPlayer instance=${pp.nextPlayer}, prevPlayer instance=${pp.prevPlayer}")
                        // Hoán đổi trong adapter
                        adapter.value!!.attachPlayerViews(position, pp)
                        CoroutineScope(Dispatchers.Main).launch {
                            launch {
                                // Phát currentPlayer
                                pp.currentPlayer.apply {
                                    playForCurrent(isFirstTime = true, forward = forward, source = mediaSources[position])
                                }
                            }
                            launch {
                                // Chuẩn bị nextPlayer
                                if (position + 1 < mediaSources.size) {
                                    pp.nextPlayer.apply {
                                        prepareForNext(mediaSources[position + 1])
                                    }
                                }
                            }
                            launch {
                                // Tạm dừng prevPlayer
                                if (position - 1 >= 0) {
                                    pp.prevPlayer.apply {
                                        pauseForPrev(mediaSources[position - 1])
                                    }
                                }
                            }
                        }
                        lastPos.intValue = position
                    }
                })
            }
        }
    )
}

@UnstableApi
private fun updateSurfaceHolders(pp: PlayerPool) {
    val tag = "ReelList"
    // Cập nhật surfaceHolders cho prevPlayer
    if (pp.prevPlayerView.videoSurfaceView is SurfaceView) {
        // Clear SurfaceHolder cũ
        pp.surfaceHolders[pp.prevPlayer]?.let { oldHolder ->
            pp.prevPlayer.clearVideoSurfaceHolder(oldHolder)
            Timber.tag(tag).d("Cleared old SurfaceHolder for prevPlayer: %s", pp.prevPlayer)
        }
        pp.surfaceHolders.remove(pp.prevPlayer)
        // Set SurfaceHolder mới
        val prevHolder = (pp.prevPlayerView.videoSurfaceView as SurfaceView).holder
        pp.surfaceHolders[pp.prevPlayer] = prevHolder
        pp.prevPlayer.setVideoSurfaceHolder(prevHolder)
        Timber.tag(tag).d("Updated SurfaceHolder for prevPlayer: %s", pp.prevPlayer)
    } else {
        Timber.tag(tag).d("No SurfaceHolder for prevPlayer: player=%s, playerView=%s", pp.prevPlayer, pp.prevPlayerView)
    }
    // Cập nhật surfaceHolders cho currentPlayer
    if (pp.currentPlayerView.videoSurfaceView is SurfaceView) {
        // Clear SurfaceHolder cũ
        pp.surfaceHolders[pp.currentPlayer]?.let { oldHolder ->
            pp.currentPlayer.clearVideoSurfaceHolder(oldHolder)
            Timber.tag(tag).d("Cleared old SurfaceHolder for currentPlayer: %s", pp.currentPlayer)
        }
        pp.surfaceHolders.remove(pp.currentPlayer)
        // Set SurfaceHolder mới
        val currentHolder = (pp.currentPlayerView.videoSurfaceView as SurfaceView).holder
        pp.surfaceHolders[pp.currentPlayer] = currentHolder
        pp.currentPlayer.setVideoSurfaceHolder(currentHolder)
        Timber.tag(tag).d("Updated SurfaceHolder for currentPlayer: %s", pp.currentPlayer)
    } else {
        Timber.tag(tag).d("No SurfaceHolder for currentPlayer: player=%s, playerView=%s", pp.currentPlayer, pp.currentPlayerView)
    }
    // Cập nhật surfaceHolders cho nextPlayer
    if (pp.nextPlayerView.videoSurfaceView is SurfaceView) {
        // Clear SurfaceHolder cũ
        pp.surfaceHolders[pp.nextPlayer]?.let { oldHolder ->
            pp.nextPlayer.clearVideoSurfaceHolder(oldHolder)
            Timber.tag(tag).d("Cleared old SurfaceHolder for nextPlayer: %s", pp.nextPlayer)
        }
        pp.surfaceHolders.remove(pp.nextPlayer)
        // Set SurfaceHolder mới
        val nextHolder = (pp.nextPlayerView.videoSurfaceView as SurfaceView).holder
        pp.surfaceHolders[pp.nextPlayer] = nextHolder
        pp.nextPlayer.setVideoSurfaceHolder(nextHolder)
        Timber.tag(tag).d("Updated SurfaceHolder for nextPlayer: %s", pp.nextPlayer)
    } else {
        Timber.tag(tag).d("No SurfaceHolder for nextPlayer: player=%s, playerView=%s", pp.nextPlayer, pp.nextPlayerView)
    }
}