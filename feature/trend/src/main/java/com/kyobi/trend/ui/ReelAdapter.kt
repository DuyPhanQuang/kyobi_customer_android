package com.kyobi.trend.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.kyobi.feature.trend.R
import com.kyobi.trend.model.Reel
import timber.log.Timber

@UnstableApi
class ReelAdapter(
    val reels: List<Reel>,
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    private val viewPager: ViewPager2,
    private val playbackViewModel: ReelPlaybackViewModel,
) : RecyclerView.Adapter<ReelAdapter.ReelViewHolder>() {
    private val tag = "ReelAdapter"

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        this.recyclerView = null
    }

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
        playbackViewModel.updateSurfaceReadyState(position = position, isReady = false)
        playbackViewModel.onPlayerReleased(position)
        holder.playerView.removeAllViews()
        holder.playerView.requestLayout()
        holder.playerView.post { holder.playerView.invalidate() }
        Timber.tag(tag).d("Triggered onViewRecycled at position $position")
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        holder.bind(reels[position], position)
    }

    override fun getItemCount(): Int = reels.size

    private var recyclerView: RecyclerView? = null

    fun attachPlayerViews(position: Int, pool: PlayerPool) {
        // Gắn PlayerView vào các vị trí tương ứng
        attachPlayerViewAt(position - 1, pool.prevPlayerView)
        attachPlayerViewAt(position, pool.currentPlayerView)
        attachPlayerViewAt(position + 1, pool.nextPlayerView)
        Timber.tag(tag).d("attachPlayerViews at position %d, position=$position")
        val holder = findViewHolderForAdapterPosition(position)
        holder?.setupCurrentPlayerListener(pool.currentPlayer)
    }

    fun attachPlayerViewAt(position: Int, playerView: PlayerView) {
        val holder = findViewHolderForAdapterPosition(position) ?: return
        val currentParent = playerView.parent as? ViewGroup
        val targetContainer = holder.playerView
        // Nếu PlayerView chưa ở đúng container, gắn
        currentParent?.removeView(playerView)
        targetContainer.removeAllViews()
        targetContainer.addView(playerView)
        playerView.requestLayout()
        playerView.post { playerView.invalidate() }
    }

    private fun findViewHolderForAdapterPosition(position: Int): ReelViewHolder? {
        return recyclerView?.findViewHolderForAdapterPosition(position) as? ReelViewHolder
    }

    inner class ReelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerView: PlayerView = itemView.findViewById(R.id.player_view)
        private val loadingAnimation: LottieAnimationView = itemView.findViewById(R.id.loading_animation)
        private val tvReelInfo: TextView = itemView.findViewById(R.id.tv_reel_info)
        var currentPlayer: ExoPlayer? = null

        fun setupCurrentPlayerListener(player: ExoPlayer) {
            currentPlayer = player
            currentPlayer?.addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    Timber.tag("ExoPlayer").d("First frame rendered")
                }
                override fun onSurfaceSizeChanged(width: Int, height: Int) {
                    if (width > 0 && height > 0 && bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        Timber.tag(tag).d("Surface size changed for position $bindingAdapterPosition: $width x $height")
                        playbackViewModel.updateSurfaceReadyState(position = bindingAdapterPosition, isReady = true)
                    }
                }
                override fun onPlaybackStateChanged(state: Int) {
                    Timber.tag(tag).d("Playback state changed for position $bindingAdapterPosition: $state")
                    when (state) {
                        Player.STATE_BUFFERING -> showLoading()
                        Player.STATE_READY -> if (currentPlayer?.isPlaying == true) hideLoading()
                        Player.STATE_ENDED, Player.STATE_IDLE -> hideLoading()
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Timber.tag(tag).d("IsPlaying changed for position $bindingAdapterPosition: $isPlaying")
                    if (isPlaying && player.playbackState == Player.STATE_READY) {
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