package com.kyobi.trend.ui

import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import coil.compose.AsyncImage
import com.kyobi.trend.extension.pauseForPrev
import com.kyobi.trend.extension.playForCurrent
import com.kyobi.trend.extension.prepareForNext
import com.kyobi.trend.extension.resetNextBeforeReuse
import com.kyobi.trend.extension.resetPrevBeforeReuse
import com.kyobi.trend.model.Reel
import timber.log.Timber

@UnstableApi
@Composable
fun ReelList(
    initReels: List<Reel>,
    topSystemBarHeight: Dp = 0.dp,
    bottomNavBarHeight: Dp = 0.dp,
    viewModel: ReelPlaybackViewModel = hiltViewModel()
) {
    val tag = "ReelList"
    val context = LocalContext.current
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { initReels.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.setReels(initReels)
        viewModel.initPlayerPool()
    }

    LaunchedEffect(pagerState) {
        snapshotFlow {
            pagerState.settledPage to pagerState.isScrollInProgress
        }.collect { (settledPage, isScrolling) ->
            if (!isScrolling) {
                Timber.tag(tag).d("snapshotFlow triggered: settledPage=$settledPage, isScrollInProgress=$isScrolling")
                viewModel.onPageSelected(settledPage)
                val pool = viewModel.playerPool.value ?: return@collect
                val mediaSource = viewModel.getMediaSource(settledPage)

                // Swap logic
                if (settledPage > viewModel.currentPosition.intValue) { // Forward
                    pool.prevPlayer.resetPrevBeforeReuse()
                    val oldPrev = pool.prevPlayer
                    pool.prevPlayer = pool.currentPlayer
                    pool.currentPlayer = pool.nextPlayer
                    pool.nextPlayer = oldPrev
                    val oldPrevView = pool.prevPlayerView
                    pool.prevPlayerView = pool.currentPlayerView
                    pool.currentPlayerView = pool.nextPlayerView
                    pool.nextPlayerView = oldPrevView
                    // Update PlayerView.player
                    pool.prevPlayerView.player = pool.prevPlayer
                    pool.currentPlayerView.player = pool.currentPlayer
                    pool.nextPlayerView.player = pool.nextPlayer
                    pool.currentPlayerView.requestLayout()
                    pool.currentPlayerView.invalidate()
                } else if (settledPage < viewModel.currentPosition.intValue) { // Backward
                    pool.nextPlayer.resetNextBeforeReuse()
                    val oldNext = pool.nextPlayer
                    pool.nextPlayer = pool.currentPlayer
                    pool.currentPlayer = pool.prevPlayer
                    pool.prevPlayer = oldNext
                    val oldNextView = pool.nextPlayerView
                    pool.nextPlayerView = pool.currentPlayerView
                    pool.currentPlayerView = pool.prevPlayerView
                    pool.prevPlayerView = oldNextView
                    // Update PlayerView.player
                    pool.prevPlayerView.player = pool.prevPlayer
                    pool.currentPlayerView.player = pool.currentPlayer
                    pool.nextPlayerView.player = pool.nextPlayer
                    pool.currentPlayerView.requestLayout()
                    pool.currentPlayerView.invalidate()
                }

                // Update Player states
                if (settledPage - 1 >= 0) {
                    val prevSource = viewModel.getMediaSource(settledPage - 1)
                    pool.prevPlayer.pauseForPrev(prevSource)
                    Timber.tag(tag).d("pauseForPrev: page=${settledPage - 1}, mediaItem=${prevSource?.mediaItem?.mediaId}")
                }
                if (mediaSource != null) {
                    pool.currentPlayer.playForCurrent(
                        isFirstTime = settledPage == 0,
                        source = mediaSource
                    )
                    Timber.tag(tag).d("playForCurrent: page=$settledPage, mediaItem=${mediaSource.mediaItem?.mediaId}, playerState=${pool.currentPlayer.playbackState}, isPlaying=${pool.currentPlayer.isPlaying}")
                }
                if (settledPage + 1 < initReels.size) {
                    val nextSource = viewModel.getMediaSource(settledPage + 1)
                    pool.nextPlayer.prepareForNext(nextSource)
                    Timber.tag(tag).d("prepareForNext: page=${settledPage + 1}, mediaItem=${nextSource?.mediaItem?.mediaId}")
                }
                viewModel.currentPosition.intValue = settledPage
                Timber.tag(tag).d("Page $settledPage: prevPlayer=${pool.prevPlayer}, video=${settledPage - 1}, currentPlayer=${pool.currentPlayer}, video=$settledPage, nextPlayer=${pool.nextPlayer}, video=${settledPage + 1}, mediaItem=${pool.currentPlayer.currentMediaItem?.mediaId}, settledPage=$settledPage")
            }
        }
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomNavBarHeight),
        key = { it }
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            val pool by viewModel.playerPool
            val playerView by rememberUpdatedState(pool?.getPlayerViewForPosition(page, pagerState.settledPage))
            var showThumbnail by remember(page) { mutableStateOf(true) }

            if (playerView != null && page == pagerState.settledPage) {
                val currentPlayer by rememberUpdatedState(
                    when (page) {
                        pagerState.settledPage -> pool?.currentPlayer
                        else -> null
                    }
                )
                AndroidView(
                    factory = {
                        // Gỡ PlayerView khỏi parent cũ nếu có
                        (playerView!!.parent as? ViewGroup)?.removeView(playerView)
                        playerView!!.player = currentPlayer
                        playerView!!
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        if (view.player != currentPlayer) {
                            view.player = currentPlayer
                            view.requestLayout()
                            view.invalidate()
                        }
                        view.visibility = View.VISIBLE
                        view.bringToFront()
                        Timber.tag(tag).d("PlayerView updated for page $page, player=$currentPlayer, isShown=${view.isShown}, visibility=${view.visibility}, surface=${view.videoSurfaceView?.isShown}, getPlayerViewForPosition=$playerView, mediaItem=${currentPlayer?.currentMediaItem?.mediaId}, settledPage=${pagerState.settledPage}")
                    }
                )
                LaunchedEffect(page, currentPlayer, pagerState.settledPage) {
                    currentPlayer?.addListener(object : Player.Listener {
                        override fun onRenderedFirstFrame() {
                            if (page == pagerState.settledPage) {
                                showThumbnail = false
                                Timber.tag(tag).d("First frame rendered for page $page, showThumbnail=$showThumbnail, settledPage=${pagerState.settledPage}")
                            }
                            currentPlayer?.removeListener(this)
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            Timber.tag(tag).e(error, "Player error for page $page, settledPage=${pagerState.settledPage}")
                            viewModel.getMediaSource(page)?.let { source ->
                                currentPlayer?.clearMediaItems()
                                currentPlayer?.setMediaSource(source)
                                currentPlayer?.prepare()
                                currentPlayer?.playWhenReady = true
                            }
                        }
                    })
                }
            }

            if (showThumbnail && initReels.isNotEmpty() && initReels[page].thumbnailUrl?.isNotEmpty() == true) {
                AsyncImage(
                    model = initReels[page].thumbnailUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}