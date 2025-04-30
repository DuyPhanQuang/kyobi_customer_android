package com.kyobi.trend.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
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
    val pendingPlayPositions = remember { mutableSetOf<Int>() } // Lưu các position chờ phát
    var lastPlayedPosition by remember { mutableIntStateOf(-1) } // Theo dõi position cuối cùng đã phát
    var currentSnappedPosition by remember { mutableIntStateOf(-1) } // Theo dõi position hiện tại dựa trên snap

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
                    onPlayerReady = { position, player, isSurfaceReady ->
                        // Khi player sẵn sàng, kiểm tra xem position này có đang chờ phát không
                        if (pendingPlayPositions.contains(position)) {
                            // Chỉ phát nếu position này là position hiện tại được snap
                            if (position == currentSnappedPosition) {
                                playbackViewModel.playVideoAtPosition(position, player, isSurfaceReady)
                                lastPlayedPosition = position
                                pendingPlayPositions.remove(position)
                                Timber.tag(tag).d("Played video at position $position after player ready")
                            } else {
                                Timber.tag(tag).d("Player ready for position $position, but not current snapped position ($currentSnappedPosition), keeping in pending")
                            }
                        }
                    }
                )
                this.adapter = adapter.value
                offscreenPageLimit = 1 // Giới hạn preload 1 item trước/sau
                playbackViewModel.setReels(currentReels)
                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                        super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                        // Tính toán position hiện tại dựa trên vị trí snap
                        val newPosition = if (positionOffset < 0.5f) position else position + 1
                        if (newPosition != currentSnappedPosition && newPosition >= 0 && newPosition < currentReels.size) {
                            Timber.tag(tag).d("Snapped to position: $newPosition (offset: $positionOffset)")
                            currentSnappedPosition = newPosition
                        }
                    }
                    override fun onPageSelected(position: Int) {
                        super.onPageSelected(position)
                        Timber.tag(tag).d("Page selected: $position")
                        if (position != lastPlayedPosition && currentReels.isNotEmpty()) {
                            val recyclerView = getChildAt(0) as? RecyclerView
                            if (recyclerView != null) {
                                val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelAdapter.ReelViewHolder
                                holder?.let { h ->
                                    if (h.player != null) {
                                        // Phát video khi page đã được snap hoàn toàn
                                        playbackViewModel.playVideoAtPosition(position, h.player!!, h.isSurfaceReady)
                                        lastPlayedPosition = position
                                        pendingPlayPositions.remove(position)
                                        Timber.tag(tag).d("Playing video at position $position after page selected")
                                    } else {
                                        // Nếu player chưa sẵn sàng, thêm vào danh sách chờ
                                        pendingPlayPositions.add(position)
                                        Timber.tag(tag).d("Player not ready for position $position, added to pending play")
                                    }
                                } ?: run {
                                    Timber.tag(tag).w("Holder not found for position $position, skipping play")
                                }
                            } else {
                                Timber.tag(tag).w("RecyclerView not found in ViewPager2, skipping play at position $position")
                            }
                        }
                    }
                })
            }
        },
        update = { viewPager ->
            (viewPager.adapter as? ReelAdapter)?.let { reelAdapter ->
                if (reelAdapter.reels != currentReels) {
                    playbackViewModel.setReels(currentReels)
                    viewPager.post {
                        if (playbackViewModel.getCurrentPlayingPosition() == -1 && currentReels.isNotEmpty()) {
                            val recyclerView = viewPager.getChildAt(0) as? RecyclerView
                            if (recyclerView != null) {
                                val holder = recyclerView.findViewHolderForAdapterPosition(0) as? ReelAdapter.ReelViewHolder
                                holder?.let { h->
                                    if (h.player != null) {
                                        // Nếu player đã sẵn sàng, phát ngay
                                        playbackViewModel.playVideoAtPosition(0, h.player!!, h.isSurfaceReady)
                                        lastPlayedPosition = 0
                                        pendingPlayPositions.remove(0)
                                        Timber.tag(tag).d("Playing video at position 0 during update")
                                    } else {
                                        // Nếu player chưa sẵn sàng, thêm vào danh sách chờ
                                        pendingPlayPositions.add(0)
                                        Timber.tag(tag).d("Player not ready for position 0 during update, added to pending play")
                                    }
                                } ?: run {
                                    Timber.tag(tag).w("Holder not found for position 0 during update")
                                }
                            } else {
                                Timber.tag(tag).w("RecyclerView not found in ViewPager2 during update")
                            }
                        } else {
                            Timber.tag(tag).d("Video at position 0 is already playing, skipping play")
                        }
                    }
                }
            }
        }
    )
}