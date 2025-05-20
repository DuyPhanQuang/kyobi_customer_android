package com.kyobi.trend.ui

import androidx.annotation.OptIn
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.media3.common.util.UnstableApi
import coil.ImageLoader
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.kyobi.feature.trend.R
import com.kyobi.theme.kyobiTheme

@OptIn(UnstableApi::class)
@Composable
fun ReelList(
    topSystemBarHeight: Dp = 0.dp,
    bottomNavBarHeight: Dp = 0.dp,
    viewModel: ReelPlaybackViewModel,
    imageLoader: ImageLoader
) {
    val tag = "ReelList"
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { viewModel.reels.value.size }
    )
    val isVideoProcessing by viewModel.isVideoProcessing.collectAsState()
    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(1),
        snapAnimationSpec = tween(
            durationMillis = 250,
            easing = { fraction -> 1f - (1f - fraction) * (1f - fraction) } // EaseOutQuadEasing
        ),
        snapPositionalThreshold = 0.35f
    )
    val lottieComposition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.reel_loading_spinner))

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = bottomNavBarHeight),
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            VerticalPager(
                modifier = Modifier.zIndex(0f),
                state = pagerState,
                flingBehavior = fling,
                userScrollEnabled = !isVideoProcessing,
                key = { it }
            ) { page ->
                Box(modifier = Modifier.fillMaxSize()) {
                    ReelVideoPlayer(
                        pagerState = pagerState,
                        pageIndex = page,
                        viewModel = viewModel,
                        imageLoader = imageLoader,
                        onSingleTap = { exoPlayer ->
                            exoPlayer.playWhenReady = !exoPlayer.isPlaying
                        },
                    )
                }
            }
            if (isVideoProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                        .zIndex(1f),
                    contentAlignment = Alignment.Center
                ) {
                    LottieAnimation(
                        composition = lottieComposition,
                        modifier = Modifier
                            .size(MaterialTheme.kyobiTheme.width.dp68),
                        iterations = LottieConstants.IterateForever
                    )
                }
            }
        }
    }
}