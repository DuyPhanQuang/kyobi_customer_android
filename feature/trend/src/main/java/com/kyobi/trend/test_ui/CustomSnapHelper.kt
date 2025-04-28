package com.kyobi.trend.test_ui

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView

class CenterSnapHelper : PagerSnapHelper() {

    override fun findTargetSnapPosition(layoutManager: RecyclerView.LayoutManager, velocityX: Int, velocityY: Int): Int {
        if (layoutManager !is LinearLayoutManager) return RecyclerView.NO_POSITION

        val currentView = findSnapView(layoutManager) ?: return RecyclerView.NO_POSITION
        val currentPosition = layoutManager.getPosition(currentView)

        if (currentPosition == RecyclerView.NO_POSITION) return RecyclerView.NO_POSITION

        return when {
            velocityY > 400 -> minOf(currentPosition + 1, layoutManager.itemCount - 1)
            velocityY < -400 -> maxOf(currentPosition - 1, 0)
            else -> currentPosition
        }
    }

    override fun calculateDistanceToFinalSnap(
        layoutManager: RecyclerView.LayoutManager,
        targetView: View
    ): IntArray? {
        if (layoutManager !is LinearLayoutManager) return super.calculateDistanceToFinalSnap(layoutManager, targetView)

        val out = IntArray(2)
        out[1] = distanceToCenter(layoutManager, targetView, layoutManager.orientation)
        out[0] = 0
        return out
    }

    private fun distanceToCenter(layoutManager: RecyclerView.LayoutManager, targetView: View, orientation: Int): Int {
        val center = if (orientation == RecyclerView.VERTICAL) {
            layoutManager.height / 2
        } else {
            layoutManager.width / 2
        }
        val childCenter = (targetView.top + targetView.bottom) / 2
        return childCenter - center
    }
}
