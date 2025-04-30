package com.kyobi.trend.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.kyobi.feature.trend.R
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@UnstableApi
class ReelAdapter(
    val reels: List<Reel>,
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    private val viewPager: ViewPager2,
    private val playbackViewModel: ReelPlaybackViewModel,
    private val onPlayerReady: (position: Int, player: ExoPlayer, isSurfaceReady: Boolean) -> Unit
) : RecyclerView.Adapter<ReelAdapter.ReelViewHolder>() {
    private val tag = "ReelAdapter"
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reel, parent, false)
        view.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        view.setPadding(0, 0, 0, 0)
        return ReelViewHolder(view)
    }

    override fun onViewRecycled(holder: ReelViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION && position != playbackViewModel.getCurrentPlayingPosition()) {
            coroutineScope.launch {
                withContext(Dispatchers.Default) { // Chuyển tác vụ nặng sang background thread
                    holder.player?.let { player ->
                        if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                            player.volume = 0f
                            player.pause()
                            Timber.tag(tag).d("Paused player at position $position during onViewRecycled")
                        }
                        player.repeatMode = Player.REPEAT_MODE_OFF
                        player.stop()
                        player.clearMediaItems()
                        Timber.tag(tag).d("Cleared player at position $position during onViewRecycled")
                        // Đặt player của PlayerView về null để xóa surface
                        holder.playerView.player = null
                        holder.player = null
                    }
                    holder.isSurfaceReady = false
                    holder.hideLoading()
                    withContext(Dispatchers.Main) { // Quay lại main thread để cập nhật UI và ViewModel
                        playbackViewModel.updateSurfaceReadyState(position = position, isReady = false)
                        playbackViewModel.onPlayerReleased(position)
                        playbackViewModel.removePlayerView(position)
                        holder.playerView.requestLayout()
                        holder.playerView.invalidate()
                    }
                }
            }
        }
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        holder.bind(reels[position], position)
        // Đảm bảo PlayerView được xóa sạch trước khi gán player mới
        holder.playerView.player = null
        holder.player = null
        // Lấy ExoPlayer từ ReelPlaybackViewModel
        val player = playbackViewModel.getOrCreatePlayerForPosition(position)
        holder.player = player
        holder.playerView.player = player
        configPlayerView(holder.playerView)
        holder.setupPlayerListener()
        // Lưu PlayerView vào ViewModel
        playbackViewModel.setPlayerView(position, holder.playerView)
        // Kiểm tra xem surface đã sẵn sàng chưa
        val isSurfaceReady = holder.playerView.width > 0 && holder.playerView.height > 0
        holder.isSurfaceReady = isSurfaceReady
        Timber.tag(tag).d("Surface ready on bind for position $position: $isSurfaceReady")
        // Chuẩn bị ExoPlayer để đảm bảo surface được tạo
        player.prepare()
        // Buộc PlayerView làm mới surface
        holder.playerView.requestLayout()
        holder.playerView.invalidate()
        holder.playerView.post {
            Timber.tag(tag).d("PlayerView size for position $position: ${holder.playerView.width}x${holder.playerView.height}")
            Timber.tag(tag).d("Cleared and invalidated PlayerView on recycle for position ${holder.bindingAdapterPosition}")
        }
        // Callback player đã sẵn sàng
        onPlayerReady(position, holder.player!!, holder.isSurfaceReady)
        Timber.tag(tag).d("Player initialized for position $position")
    }

    override fun getItemCount(): Int = reels.size

    private fun configPlayerView(playerView: PlayerView) {
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
        playerView.setBackgroundColor(Color.TRANSPARENT)
        playerView.setKeepContentOnPlayerReset(true)
        playerView.setUseController(false)
        playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
        playerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
    }

    inner class ReelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerView: PlayerView = itemView.findViewById(R.id.player_view)
        private val loadingAnimation: LottieAnimationView = itemView.findViewById(R.id.loading_animation)
        private val tvReelInfo: TextView = itemView.findViewById(R.id.tv_reel_info)
        var player: ExoPlayer? = null
        var isSurfaceReady = false

        fun setupPlayerListener() {
            player?.addListener(object : Player.Listener {
                override fun onSurfaceSizeChanged(width: Int, height: Int) {
                    if (width > 0 && height > 0 && bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        isSurfaceReady = true
                        Timber.tag(tag).d("Surface ready for position $bindingAdapterPosition: $width x $height")
                        // Thông báo cho ReelPlaybackViewModel rằng surface đã sẵn sàng
                        playbackViewModel.updateSurfaceReadyState(position = bindingAdapterPosition, isReady = true)
                    }
                }
                override fun onPlaybackStateChanged(state: Int) {
                    Timber.tag(tag).d("Playback state changed for position $bindingAdapterPosition: $state")
                    when (state) {
                        Player.STATE_BUFFERING -> showLoading()
                        Player.STATE_READY -> if (player?.isPlaying == true) hideLoading()
                        Player.STATE_ENDED, Player.STATE_IDLE -> hideLoading()
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Timber.tag(tag).d("IsPlaying changed for position $bindingAdapterPosition: $isPlaying")
                    if (isPlaying && player?.playbackState == Player.STATE_READY) {
                        hideLoading()
                    }
                }
            })
        }

        fun showLoading() {
            loadingAnimation.visibility = View.VISIBLE
            loadingAnimation.playAnimation()
            Timber.tag(tag).d("Showing loading animation for position $bindingAdapterPosition")
        }

        fun hideLoading() {
            loadingAnimation.visibility = View.GONE
            loadingAnimation.cancelAnimation()
            Timber.tag(tag).d("Hiding loading animation for position $bindingAdapterPosition")
        }

        fun bind(reel: Reel, position: Int) {
            Timber.tag(tag).d("Binding position: $position")
            tvReelInfo.text = """
                ID: ${reel.id}
            """.trimIndent()
        }
    }
}