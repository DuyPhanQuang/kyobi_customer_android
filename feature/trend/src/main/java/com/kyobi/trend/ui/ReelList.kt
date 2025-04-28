package com.kyobi.trend.ui

import android.widget.EdgeEffect
import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.featurecommon.monitor.network.NetworkUtils
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.config.ReelConfigViewModel
import com.kyobi.trend.model.Reel
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import com.kyobi.trend.test_ui.CenterSnapHelper
import timber.log.Timber
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun ReelList(
    reels: List<Reel>,
    mediaCache: MediaCache,
    recyclerViewRef: MutableState<RecyclerView?>? = null,
    topSystemBarHeight: Dp = Dp(0f),
    bottomNavBarHeight: Dp = Dp(0f)
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configViewModel: ReelConfigViewModel = hiltViewModel()
    val networkUtils = remember { NetworkUtils(context) }
    val networkMonitor = remember { NetworkMonitor(context, networkUtils) }
    val currentReels by rememberUpdatedState(reels)
    val adapter = remember { mutableStateOf<ReelAdapter?>(null) }

    // Observer network change
    val isConnected by networkMonitor.observeNetwork { isConnected ->
        if (isConnected) {
            Timber.tag("ReelList").d("Network connected, retrying downloads")
            adapter.value?.retryDownloads()
        } else {
            Timber.tag("ReelList").d("Network disconnected")
        }
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 0.dp, bottom = bottomNavBarHeight),
        factory = { context2 ->
            RecyclerView(context2).apply {
                layoutManager = LinearLayoutManager(context2, RecyclerView.VERTICAL, false).apply {
                    initialPrefetchItemCount = 5
                }
                // khởi tạo và gán adapter
                adapter.value = ReelAdapter(
                    reels = currentReels,
                    context = context2,
                    mediaCache = mediaCache,
                    lifecycleOwner = lifecycleOwner,
                    configViewModel = configViewModel,
                    networkMonitor = networkMonitor,
                    recyclerView = this
                )
                this.adapter = adapter.value
                setHasFixedSize(true)
                // tối ưu performance bằng cách:
                // - Tăng cache để tái sử dụng view
                // - Tăng số view tái sử dụng
                setItemViewCacheSize(5)
                setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
                    setMaxRecycledViews(0, 5)
                })
                val snapHelper = CenterSnapHelper()
                snapHelper.attachToRecyclerView(this)

                // hiệu ứng kéo-nhả giống TikTok (ở đầu/cuối danh sách)
                edgeEffectFactory = object : RecyclerView.EdgeEffectFactory() {
                    override fun createEdgeEffect(recyclerView: RecyclerView, direction: Int): EdgeEffect {
                        return object : EdgeEffect(recyclerView.context) {
                            override fun onPull(deltaDistance: Float) {
                                super.onPull(deltaDistance)
                                recyclerView.translationY = deltaDistance * recyclerView.height * 0.2f
                            }
                            override fun onRelease() {
                                super.onRelease()
                                recyclerView.animate().translationY(0f).setDuration(200).start()
                            }
                        }
                    }
                }

                // auto play video when snap
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    private var lastPlayTime = 0L // thời gian lần gần nhất play
                    private val playDebounceDuration = 200L
                    private var lastScrollTime = 0L
                    private val scrollDebounceDuration = 100L // thời gian lần gần nhất scroll

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
                        val currentTime = System.currentTimeMillis()
                        if (currentTime - lastScrollTime < scrollDebounceDuration) {
                            return
                        }
                        lastScrollTime = currentTime

                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                        if (firstVisiblePosition == RecyclerView.NO_POSITION || lastVisiblePosition == RecyclerView.NO_POSITION) {
                            return
                        }

                        val currentPlayingPosition = adapter.value?.currentPlayingPosition
                        // Xác định vị trí video "nổi bật" để play
                        // vị trí nổi bật là vị trí gần trung tâm nhất
                        val targetPosition = determineProminentPosition(recyclerView, firstVisiblePosition, lastVisiblePosition)
                        // Debounce: Chỉ play nếu đã qua 200ms kể từ lần play trước
                        if (targetPosition != RecyclerView.NO_POSITION &&
                            targetPosition != currentPlayingPosition &&
                            (currentTime - lastPlayTime) > playDebounceDuration) {
                            // Auto play video với vị trí nổi bật
                            adapter.value?.playVideoAtPosition(targetPosition)
                            lastPlayTime = currentTime
                        }

                        // Preload videos
                        (recyclerView.adapter as? ReelAdapter)?.preloadVideos(firstVisiblePosition, lastVisiblePosition)
                    }

                    // Hàm xác định vị trí video "nổi bật" (gần trung tâm màn hình nhất)
                    private fun determineProminentPosition(recyclerView: RecyclerView, firstVisiblePosition: Int, lastVisiblePosition: Int): Int {
                        val screenHeight = recyclerView.height
                        val centerY = screenHeight / 2
                        var closestPosition = RecyclerView.NO_POSITION
                        var minDistanceToCenter = Int.MAX_VALUE

                        for (position in firstVisiblePosition..lastVisiblePosition) {
                            val view = recyclerView.findViewHolderForAdapterPosition(position)?.itemView ?: continue
                            val viewCenterY = (view.top + view.bottom) / 2
                            val distanceToCenter = abs(viewCenterY - centerY)
                            if (distanceToCenter < minDistanceToCenter) {
                                minDistanceToCenter = distanceToCenter
                                closestPosition = position
                            }
                        }
                        return closestPosition
                    }
                })
                recyclerViewRef?.value = this
                post {
                    adapter.value?.let { reelAdapter ->
                        if (reelAdapter.currentPlayingPosition == RecyclerView.NO_POSITION) {
                            reelAdapter.playVideoAtPosition(0)
                        } else {
                            Timber.tag("ReelList").d("Video at position 0 is already playing, skipping play")
                        }
                    }
                }
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? ReelAdapter)?.let { reelAdapter ->
                if (reelAdapter.reels != currentReels) {
                    recyclerView.post {
                        if (reelAdapter.currentPlayingPosition == RecyclerView.NO_POSITION) {
                            reelAdapter.playVideoAtPosition(0)
                        } else {
                            Timber.tag("ReelList").d("Video at position 0 is already playing, skipping play")
                        }
                    }
                }
            }
        },
        onRelease = { recyclerView ->
            (recyclerView.adapter as? ReelAdapter)?.releaseAllPlayers()
            recyclerView.adapter = null
            adapter.value = null
            recyclerViewRef?.value = null
        }
    )

    DisposableEffect(Unit) {
        onDispose {
            adapter.value?.releaseAllPlayers()
            adapter.value = null
            recyclerViewRef?.value?.adapter = null
            recyclerViewRef?.value = null
        }
    }
}