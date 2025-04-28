package com.kyobi.trend.test_ui

import android.annotation.SuppressLint
import android.widget.EdgeEffect
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

@SuppressLint("NotifyDataSetChanged")
@Composable
fun ReelListNew(
    topSystemBarHeight: Dp = Dp(0f),
    bottomNavBarHeight: Dp = Dp(0f)
) {
    val context = LocalContext.current
    val items = remember {
        List(100) { index ->
            TestReel(
                id = "test_reel_$index",
                title = "Test Reel $index"
            )
        }
    }
    val currentItems by rememberUpdatedState(items)
    val adapter = remember { mutableStateOf<ReelAdapterNew?>(null) }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 0.dp, bottom = bottomNavBarHeight),
        factory = { context2 ->
            RecyclerView(context2).apply {
                layoutManager = LinearLayoutManager(context2, RecyclerView.VERTICAL, false).apply {
                    initialPrefetchItemCount = 5
                }

                adapter.value = ReelAdapterNew(currentItems, context2)
                this.adapter = adapter.value
                setHasFixedSize(true)
                setItemViewCacheSize(5)
                setRecycledViewPool(RecyclerView.RecycledViewPool().apply {
                    setMaxRecycledViews(0, 5)
                })

                // Attach SnapHelper để bắt đúng 1 video giữa màn
                CenterSnapHelper().attachToRecyclerView(this)

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
            }
        },
        update = { recyclerView ->
            (recyclerView.adapter as? ReelAdapterNew)?.let { reelAdapter ->
                if (reelAdapter.items != currentItems) {
                    recyclerView.post {
                        reelAdapter.notifyDataSetChanged()
                    }
                }
            }
        },
        onRelease = { recyclerView ->
            recyclerView.adapter = null
            adapter.value = null
        }
    )
}