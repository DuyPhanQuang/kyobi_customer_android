package com.kyobi.trend.ui

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.trend.KyobiSnapHelper
import com.kyobi.featurecommon.monitor.network.NetworkUtils
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.config.ReelConfigViewModel
import com.kyobi.trend.model.Reel
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import timber.log.Timber
import kotlin.math.abs

@OptIn(UnstableApi::class)
@Composable
fun ReelList(
    reels: List<Reel>,
    mediaCache: MediaCache,
    recyclerViewRef: MutableState<RecyclerView?>? = null
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
        factory = { context2 ->
            RecyclerView(context2).apply {
                layoutManager = object : LinearLayoutManager(context2, VERTICAL, false) {
                    override fun smoothScrollToPosition(
                        recyclerView: RecyclerView,
                        state: RecyclerView.State,
                        position: Int) {
                        val smoothScroller = object : LinearSmoothScroller(recyclerView.context) {
                            override fun calculateSpeedPerPixel(displayMetrics: android.util.DisplayMetrics): Float {
                                return 90f / displayMetrics.densityDpi // Tốc độ cuộn chậm, mượt hơn
                            }
                            override fun calculateTimeForDeceleration(dx: Int): Int {
                                return 80 // Thời gian giảm tốc cố định, tạo cảm giác mượt
                            }
                            override fun getVerticalSnapPreference(): Int {
                                return SNAP_TO_START // Đảm bảo snap vào đầu view
                            }
                            override fun calculateDtToFit(
                                viewStart: Int,
                                viewEnd: Int,
                                boxStart: Int,
                                boxEnd: Int,
                                snapPreference: Int
                            ): Int {
                                // Đảm bảo dừng chính xác tại vị trí mong muốn, không nhún
                                return boxStart - viewStart
                            }
                        }
                        smoothScroller.targetPosition = position
                        startSmoothScroll(smoothScroller)
                    }
                    override fun scrollVerticallyBy(dy: Int, recycler: RecyclerView.Recycler?, state: RecyclerView.State?): Int {
                        return super.scrollVerticallyBy(dy, recycler, state)
                    }
                } .apply {
                    // Thiết lập số lượng item prefetch ban đầu (thay thế cho getExtraLayoutSpace)
                    initialPrefetchItemCount = 5// Prefetch 5 item để cuộn mượt
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
                val snapHelper = KyobiSnapHelper()
                snapHelper.attachToRecyclerView(this)
                // auto play video when snap
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    private var lastVisiblePosition = -1 // Lưu visiblePosition để tránh xử lý lặp lại
                    private var isProgrammaticScroll = false // Biến để kiểm soát cuộn tự động
                    private var lastPlayTime = 0L // thời gian lần gần nhất play
                    private val playDebounceDuration = 300L
                    private var lastScrollTime = 0L
                    private val scrollDebounceDuration = 100L // thời gian lần gần nhất scroll

                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE && !isProgrammaticScroll) {
                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
                            val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
                            if (firstVisiblePosition == RecyclerView.NO_POSITION ||
                                lastVisiblePosition == RecyclerView.NO_POSITION) {
                                Timber.tag("ReelList").w("No visible position found")
                                return
                            }
                            var visiblePosition = firstVisiblePosition
                            var closestDistanceToCenter = Int.MAX_VALUE
                            // Tìm view gần trung tâm nhất
                            for (i in firstVisiblePosition..lastVisiblePosition) {
                                val child = layoutManager.findViewByPosition(i) ?: continue
                                val viewCenter = (child.top + child.bottom) / 2
                                val containerCenter = recyclerView.height / 2
                                val distanceToCenter = abs(viewCenter - containerCenter)
                                if (distanceToCenter < closestDistanceToCenter) {
                                    closestDistanceToCenter = distanceToCenter
                                    visiblePosition = i
                                }
                            }
                            // Kiểm tra xem view có cần snap không
                            // set ngưỡng để snap nhạy hơn
                            // // TikTok: ~15-20% chiều cao màn hình
                            val snapThreshold = recyclerView.height / 5
                            val snapView = layoutManager.findViewByPosition(visiblePosition)
                            if (snapView != null) {
                                val viewCenter = (snapView.top + snapView.bottom) / 2
                                val containerCenter = recyclerView.height / 2
                                val distanceToCenter = abs(viewCenter - containerCenter)
                                if (distanceToCenter > snapThreshold) {
                                    // Tìm vị trí gần trung tâm hơn
                                    for (i in firstVisiblePosition..lastVisiblePosition) {
                                        val child = layoutManager.findViewByPosition(i) ?: continue
                                        val childCenter = (child.top + child.bottom) / 2
                                        val childDistance = abs(childCenter - containerCenter)
                                        if (childDistance < distanceToCenter) {
                                            visiblePosition = i
                                            break
                                        }
                                    }
                                    // Chỉ gọi snap nếu vị trí thay đổi
                                    if (visiblePosition != this.lastVisiblePosition) {
                                        isProgrammaticScroll = true
                                        recyclerView.smoothScrollToPosition(visiblePosition)
                                    }
                                }
                            }
                        } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            isProgrammaticScroll = false // Reset sau khi cuộn tự động hoàn tất
                        }
                    }

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
                        // Debounce: Chỉ play nếu đã qua 300ms kể từ lần play trước
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
                        // Duyệt qua các view hiện tại trên màn hình
                        for (position in firstVisiblePosition..lastVisiblePosition) {
                            val view = recyclerView.findViewHolderForAdapterPosition(position)?.itemView
                            if (view != null) {
                                val viewTop = view.top
                                val viewBottom = view.bottom
                                val viewCenterY = (viewTop + viewBottom) / 2
                                // Tính khoảng cách từ trung tâm của view đến trung tâm màn hình
                                val distanceToCenter = abs(viewCenterY - centerY)
                                if (distanceToCenter < minDistanceToCenter) {
                                    minDistanceToCenter = distanceToCenter
                                    closestPosition = position
                                }
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