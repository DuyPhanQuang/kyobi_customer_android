package com.kyobi.trend.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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

    LaunchedEffect(Unit) {
        preloadedMediaItems.clear()
        preloadedMediaSources.clear()
        currentReels.forEachIndexed { index, reel ->
            val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                .setMediaId(reel.videoUrl).build()
            preloadedMediaItems[index] = mediaItem
            Timber.tag(tag).d("Preloaded mediaItem for position $index mediaItem: $mediaItem")
            val mediaSource = playbackViewModel.startCreateMediaSource(mediaItem)
            if (mediaSource != null) {
                preloadedMediaSources[index] = mediaSource
                Timber.tag(tag).d("Preloaded mediaSource for position $index mediaSource: $mediaSource")
            }
        }
    }

    if (currentReels.isNotEmpty() &&
        preloadedMediaItems.isNotEmpty() &&
        preloadedMediaSources.isNotEmpty()) {
        AndroidView(
            modifier = Modifier
                .background(MaterialTheme.kyobiTheme.colors.primary)
                .fillMaxSize()
                .padding(top = 0.dp, bottom = bottomNavBarHeight),
            factory = { context2 ->
                ViewPager2(context2).apply {
                    orientation = ViewPager2.ORIENTATION_VERTICAL
                    adapter.value = ReelAdapter(
                        reels = currentReels,
                        context = context2,
                        lifecycleOwner = lifecycleOwner,
                        viewPager = this,
                        playbackViewModel = playbackViewModel,
                        preloadedMediaItems = preloadedMediaItems,
                        preloadedMediaSources = preloadedMediaSources
                    )
                    this.adapter = adapter.value
                    offscreenPageLimit = offscreenPageNumber // Giới hạn preload items trước/sau
                    playbackViewModel.setReels(currentReels)
                    registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                        override fun onPageSelected(position: Int) {
                            super.onPageSelected(position)
                            playbackViewModel.onPageSelected(position)
                        }
                    })
                }
            },
            update = { viewPager ->
                (viewPager.adapter as? ReelAdapter)?.let { reelAdapter ->
                    if (reelAdapter.reels != currentReels) {
                        playbackViewModel.setReels(currentReels)
                    }
                }
            }
        )
    }
}