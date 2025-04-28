package com.kyobi.trend

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.abs
import kotlin.math.sign

class KyobiSnapHelper : LinearSnapHelper() {
    private var recyclerView: RecyclerView? = null
    private var lastSnapPosition: Int = -1
    private var lastVelocityY: Int = 0

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
        if (firstVisiblePosition == RecyclerView.NO_POSITION || lastVisiblePosition == RecyclerView.NO_POSITION) {
            return null
        }

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

        if (snapView != null) {
            if (lastSnapPosition != -1 && abs(lastVelocityY) > 3000) {
                val direction = lastVelocityY.sign
                val targetPosition = lastSnapPosition + direction
                if (targetPosition in firstVisiblePosition..lastVisiblePosition) {
                    val targetView = lm.findViewByPosition(targetPosition)
                    if (targetView != null) {
                        snapView = targetView
                    }
                }
            }

            val finalSnapPosition = lm.getPosition(snapView)
            if (finalSnapPosition != lastSnapPosition) {
                lastSnapPosition = finalSnapPosition
            }
        }
        return snapView
    }

    override fun calculateScrollDistance(velocityX: Int, velocityY: Int): IntArray {
        val out = IntArray(2)
        out[1] = (velocityY * 0.3f).toInt()
        lastVelocityY = velocityY
        return out
    }

    private fun distanceToCenter(layoutManager: RecyclerView.LayoutManager, targetView: View): Int {
        val viewCenter = (targetView.top + targetView.bottom) / 2
        val containerCenter = layoutManager.height / 2
        return viewCenter - containerCenter
    }
}
