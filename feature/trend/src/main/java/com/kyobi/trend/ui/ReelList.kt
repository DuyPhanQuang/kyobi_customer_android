package com.kyobi.trend.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.kyobi.trend.model.Reel
import com.kyobi.theme.kyobiTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
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
    val offscreenPageNumber = 6

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
                )
                this.adapter = adapter.value
                offscreenPageLimit = offscreenPageNumber // Giới hạn preload 20 item trước/sau
                playbackViewModel.setReels(currentReels)
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {
                        val mutex = Mutex()
                        if (mutex.tryLock()) {
                            try {
                                val currentPosition = currentItem
                                val range = offscreenPageNumber / 2
                                val nearbyPositions = (-range..range).map { currentPosition + it }
                                    .filter { it >= 0 && it < (adapter.value?.itemCount ?: 0) }
                                val batchMutex = Mutex()
                                nearbyPositions.chunked(1).forEach { batch ->
                                    if (batchMutex.tryLock()) {
                                        try {
                                            playbackViewModel.warmupPlayerNearbyPositions(batch)
                                            Timber.tag(tag).d("onPageScrolled: Prepared batch $batch")
                                        } finally {
                                            batchMutex.unlock()
                                        }
                                    }
                                }
                            } finally {
                                mutex.unlock()
                            }
                        }
                    }
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