package com.kyobi.trend.ui

import androidx.annotation.OptIn
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.util.UnstableApi
import com.kyobi.trend.model.Reel

@OptIn(UnstableApi::class)
@Composable
fun ReelList(
    initReels: List<Reel>,
    topSystemBarHeight: Dp = 0.dp,
    bottomNavBarHeight: Dp = 0.dp,
    viewModel: ReelPlaybackViewModel = hiltViewModel()
) {
    val tag = "ReelList"
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { initReels.size })
    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = tween(
            durationMillis = 250,
            easing = FastOutSlowInEasing
        ),
        snapPositionalThreshold = 0.35f
    )

    LaunchedEffect(Unit) {
        viewModel.setReels(initReels)
    }

    VerticalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomNavBarHeight),
        flingBehavior = fling,
        key = { it }
    ) { page ->
        Box(modifier = Modifier.fillMaxSize()) {
            VideoPlayer(
                reel = initReels[page],
                pagerState = pagerState,
                pageIndex = page,
                viewModel = viewModel,
                onSingleTap = { exoPlayer ->
                    exoPlayer.playWhenReady = !exoPlayer.isPlaying
                }
            )
        }
    }
}