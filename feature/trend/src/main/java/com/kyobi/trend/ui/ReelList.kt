package com.kyobi.trend.ui

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.trend.KyobiSnapHelper
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import timber.log.Timber
import kotlin.math.abs

/* Quản lý vòng đời:
- Release tất cả ExoPlayer và adapter khi RecyclerView bị dispose:
 * */
@OptIn(UnstableApi::class)
@Composable
fun ReelList(
    reels: List<Reel>,
    mediaCache: MediaCache,
    recyclerViewRef: MutableState<RecyclerView?>? = null
) {
    val context = LocalContext.current

    AndroidView(
        factory = { context2 ->
            RecyclerView(context2).apply {
                layoutManager = object : LinearLayoutManager(
                    context2,
                    LinearLayoutManager.VERTICAL,
                    false
                ) {
                    override fun smoothScrollToPosition(
                        recyclerView: RecyclerView,
                        state: RecyclerView.State,
                        position: Int
                    ) {
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

                    override fun scrollVerticallyBy(
                        dy: Int,
                        recycler: RecyclerView.Recycler?,
                        state: RecyclerView.State?
                    ): Int {
                        return super.scrollVerticallyBy(dy, recycler, state)
                    }
                } .apply {
                    // Thiết lập số lượng item prefetch ban đầu (thay thế cho getExtraLayoutSpace)
                    initialPrefetchItemCount = 5// Prefetch 5 item để cuộn mượt
                }

                adapter = ReelAdapter(reels, context = context2, mediaCache, this)
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
                    private var lastPlayedPosition = -1
                    private var lastVisiblePosition = -1 // Lưu visiblePosition để tránh xử lý lặp lại
                    private var isProgrammaticScroll = false // Biến để kiểm soát cuộn tự động
                    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
                    private val pendingPrefetchPositions = mutableSetOf<Int>() // Lưu các vị trí cần prefetch

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

                            // Chỉ xử lý nếu vị trí thay đổi
                            if (visiblePosition != this.lastVisiblePosition) {
                                Timber.tag("ReelList").d("Scroll state idle, visiblePosition: $visiblePosition")
                                if (visiblePosition != lastPlayedPosition) {
                                    (adapter as? ReelAdapter)?.playVideoAtPosition(visiblePosition)
                                    lastPlayedPosition = visiblePosition
                                }
                                this.lastVisiblePosition = visiblePosition
                            }

                        } else if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            isProgrammaticScroll = false // Reset sau khi cuộn tự động hoàn tất
                        }
                    }

                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        super.onScrolled(recyclerView, dx, dy)
//                        val layoutManager = recyclerView.layoutManager as LinearLayoutManager
//                        val firstVisiblePosition = layoutManager.findFirstVisibleItemPosition()
//                        val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()
//                        if (firstVisiblePosition == RecyclerView.NO_POSITION ||
//                            lastVisiblePosition == RecyclerView.NO_POSITION) {
//                            return
//                        }
//
//                        // Prefetch động: Tải trước các item(2) phía trước và phía sau
//                        val prefetchCount = 2
//                        val adapter = recyclerView.adapter ?: return
//                        val itemCount = adapter.itemCount
//
//                        val prefetchStart = (firstVisiblePosition - prefetchCount).coerceAtLeast(0)
//                        val prefetchEnd = (lastVisiblePosition + prefetchCount).coerceAtMost(itemCount - 1)
//                        for (pos in prefetchStart..prefetchEnd) {
//                            if (pos !in firstVisiblePosition..lastVisiblePosition) {
//                                pendingPrefetchPositions.add(pos)
//                            }
//                        }
//                        // Trì hoãn prefetch đến frame tiếp theo
//                        handler.removeCallbacksAndMessages(null)
//                        handler.post {
//                            pendingPrefetchPositions.forEach { pos ->
//                                recyclerView.adapter?.notifyItemChanged(pos)
//                            }
//                            pendingPrefetchPositions.clear()
//                        }
                    }
                })

                recyclerViewRef?.value = this

                post {
                    (adapter as? ReelAdapter)?.playVideoAtPosition(0)
                }
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? ReelAdapter)?.let { _ ->
                recyclerView.adapter = ReelAdapter(reels, context, mediaCache, recyclerView)
                recyclerView.post {
                    (recyclerView.adapter as? ReelAdapter)?.playVideoAtPosition(0)
                }
            }
        },
        onRelease = { recyclerView ->
            (recyclerView.adapter as? ReelAdapter)?.releaseAllPlayers()
            recyclerView.adapter = null
            recyclerViewRef?.value = null
        },
    )
}