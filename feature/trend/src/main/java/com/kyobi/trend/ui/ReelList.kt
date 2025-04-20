package com.kyobi.trend.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.trend.ReelAdapter
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import timber.log.Timber

/* Quản lý vòng đời:
- Release tất cả ExoPlayer và adapter khi RecyclerView bị dispose:
 * */
@OptIn(UnstableApi::class)
@Composable
fun ReelList(
    reels: List<Reel>,
    mediaCache: MediaCache
) {
    val context = LocalContext.current

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context2 ->
            RecyclerView(context2).apply {
                layoutManager = LinearLayoutManager(
                    context2,
                    LinearLayoutManager.VERTICAL,
                    false)
                adapter = ReelAdapter(reels, context = context2, mediaCache, this)
                setHasFixedSize(true)
                // Tối ưu performance
                setItemViewCacheSize(3)
                setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
                    setMaxRecycledViews(0, 5)
                })
                // LinearSnapHelper for snap each video fullscreen
                val snapHelper = PagerSnapHelper()
                snapHelper.attachToRecyclerView(this)
                // auto play video when snap
                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                        super.onScrollStateChanged(recyclerView, newState)
                        if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                            val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                            val visiblePosition = layoutManager.findFirstCompletelyVisibleItemPosition()
                            Timber.tag("ReelList").d("Scroll state idle, visiblePosition: $visiblePosition")
                            if (visiblePosition != -1) {
                                (adapter as? ReelAdapter)?.playVideoAtPosition(visiblePosition)
                            } else {
                                Timber.tag("ReelList").w("No completely visible position found, trying findFirstVisibleItemPosition")
                                val fallbackPosition = layoutManager.findFirstVisibleItemPosition()
                                if (fallbackPosition != -1) {
                                    (adapter as? ReelAdapter)?.playVideoAtPosition(fallbackPosition)
                                }
                            }
                        }
                    }
                })
                post {
                    (adapter as? ReelAdapter)?.playVideoAtPosition(0)
                }
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? ReelAdapter)?.let { _ ->
                recyclerView.adapter = ReelAdapter(reels, context, mediaCache, recyclerView)
            }
        },
        onRelease = { recyclerView ->
            (recyclerView.adapter as? ReelAdapter)?.releaseAllPlayers()
            recyclerView.adapter = null
        },
    )
}