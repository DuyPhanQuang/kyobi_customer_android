package com.kyobi.trend

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs

class KyobiSnapHelper: LinearSnapHelper() {
    private var recyclerView: RecyclerView? = null
    private var lastSnapPosition: Int = -1 // Lưu vị trí snap trước đó

    override fun attachToRecyclerView(recyclerView: RecyclerView?) {
        super.attachToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray {
        val out = IntArray(2)
        if (layoutManager.canScrollVertically()) {
            out[1] = distanceToCenter(layoutManager, targetView)
        } else {
            out[0] = distanceToCenter(layoutManager, targetView)
        }
        return out
    }

    override fun findSnapView(layoutManager: RecyclerView.LayoutManager): View? {
        val lm = layoutManager as? LinearLayoutManager ?: return null
        val firstVisiblePosition = lm.findFirstVisibleItemPosition()
        val lastVisiblePosition = lm.findLastVisibleItemPosition()
        if (firstVisiblePosition == RecyclerView.NO_POSITION ||
            lastVisiblePosition == RecyclerView.NO_POSITION) {
            return null
        }

        // Tìm view gần nhất với trung tâm màn hình
        var closestDistanceToCenter = Int.MAX_VALUE
        var snapView: View? = null
        for (pos in firstVisiblePosition..lastVisiblePosition) {
            val view = lm.findViewByPosition(pos) ?: continue
            val distance = abs(distanceToCenter(layoutManager, view))
            if (distance < closestDistanceToCenter) {
                closestDistanceToCenter = distance
                snapView = view
            }
        }

        // Đảm bảo snap chính xác ngay cả khi fling nhanh
        if (snapView != null) {
            val snapPosition = lm.getPosition(snapView)
            // Chỉ gọi smoothScrollToPosition nếu vị trí snap thay đổi
            if (snapPosition != lastSnapPosition) {
                lastSnapPosition = snapPosition
            }
        }
        return snapView
    }

    override fun calculateScrollDistance(velocityX: Int, velocityY: Int): IntArray {
        val out = super.calculateScrollDistance(velocityX, velocityY)
        if (recyclerView?.layoutManager?.canScrollVertically() == true) {
            // TikTok: ~0.1f-0.15f
            // Giảm quán tính fling
            val scaledVelocity = (velocityY * 0.15f).toInt()
            out[1] = scaledVelocity
        }
        return out
    }

    private fun distanceToCenter(layoutManager: RecyclerView.LayoutManager, targetView: View): Int {
        val viewCenter = (targetView.top + targetView.bottom) / 2
        val containerCenter = layoutManager.height / 2
        return viewCenter - containerCenter
    }
}