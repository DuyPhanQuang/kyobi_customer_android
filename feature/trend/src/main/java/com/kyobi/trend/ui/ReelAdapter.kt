package com.kyobi.trend.ui

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.kyobi.feature.trend.R
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import timber.log.Timber

@UnstableApi
class ReelAdapter(
    val reels: List<Reel>,
    private val context: Context,
    lifecycleOwner: LifecycleOwner,
    private val viewPager: ViewPager2,
    private val playbackViewModel: ReelPlaybackViewModel,
    private val preloadedMediaItems: Map<Int, MediaItem>,
    private val preloadedMediaSources: Map<Int, MediaSource>
) : RecyclerView.Adapter<ReelAdapter.ReelViewHolder>() {
    private val tag = "ReelAdapter"
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    init {
        playbackViewModel.onRefreshSurface = { position ->
            val holder = findViewHolderForAdapterPosition(position)
            if (holder is ReelViewHolder) {
                holder.playerViewContainer.requestLayout()
                holder.playerViewContainer.invalidate()
                Timber.tag(tag).d("Refreshed UI via onRefreshSurface called for position: $position")
            } else {
                Timber.tag(tag).d("holder not found")
            }
        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        this.recyclerView = recyclerView
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
        if (position != RecyclerView.NO_POSITION && position != playbackViewModel.getCurrentPlayingPosition()) {
            holder.player?.let { player ->
                if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                    player.volume = 0f
                    player.pause()
                    player.stop()
                    player.clearMediaItems()
                    player.release()
                    Timber.tag(tag).d("Paused Stopped Released player at position $position during onViewRecycled")
                }
            }
            holder.isSurfaceReady = false
            holder.hideLoading()
            playbackViewModel.updateSurfaceReadyState(position = position, isReady = false)
            playbackViewModel.onPlayerReleased(position)
            holder.playerViewContainer.removeAllViews()
            holder.playerViewContainer.requestLayout()
            holder.playerViewContainer.post { holder.playerViewContainer.invalidate() }
            Timber.tag(tag).d("Triggered onViewRecycled at position $position")
        }
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        holder.bind(reels[position], position)
        holder.playerViewContainer.removeAllViews()
        val preloadedMediaItem = preloadedMediaItems[position]
        val preloadedMediaSource = preloadedMediaSources[position]
        playbackViewModel.setPreloadedMediaItem(position, preloadedMediaItem)
        playbackViewModel.setPreloadedMediaSource(position, preloadedMediaSource)
        val preloadedView = playbackViewModel.getPlayerView(position)
        if (preloadedView != null) {
            holder.attachPreloadedPlayerView(preloadedView)
        } else {
            val playerView = PlayerView(context).apply {
                setBackgroundColor(Color.TRANSPARENT)
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                setKeepContentOnPlayerReset(true)
                setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
            val player = ExoPlayer.Builder(context).setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(1000, 5000, 500, 1000)
                .build()).build().also {
                Timber.tag(tag).d("Created new player for position $position")
            }
            playerView.player = player
            holder.attachPreloadedPlayerView(playerView)
            playbackViewModel.setPlayerView(position, playerView)
        }
        holder.setupPlayerListener()
        playbackViewModel.createDrawMeasureVideoAtPosition(position, holder.isSurfaceReady)
        // Cập nhật isSurfaceReady dựa trên kích thước của PlayerView đã preload
        val isSurfaceReady = preloadedView?.let { it.width > 0 && it.height > 0 } ?: false
        holder.isSurfaceReady = isSurfaceReady
        Timber.tag(tag).d("Surface ready on bind for position $position: $isSurfaceReady")
        holder.playerViewContainer.requestLayout()
        holder.playerViewContainer.post { holder.playerViewContainer.invalidate() }
        holder.playerViewContainer.post {
            Timber.tag(tag).d("PlayerView size for position $position: ${holder.playerViewContainer.width}x${holder.playerViewContainer.height}")
        }
        Timber.tag(tag).d("Player initialized for position $position")
    }

    override fun getItemCount(): Int = reels.size

    private var recyclerView: RecyclerView? = null

    private fun findViewHolderForAdapterPosition(position: Int): RecyclerView.ViewHolder? {
        return recyclerView?.findViewHolderForAdapterPosition(position)
    }

    inner class ReelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerViewContainer: FrameLayout = itemView.findViewById(R.id.player_view_container)
        private val loadingAnimation: LottieAnimationView = itemView.findViewById(R.id.loading_animation)
        private val tvReelInfo: TextView = itemView.findViewById(R.id.tv_reel_info)
        var player: ExoPlayer? = null
        var isSurfaceReady = false

        fun attachPreloadedPlayerView(preloadedView: PlayerView) {
            playerViewContainer.removeAllViews()
            playerViewContainer.addView(preloadedView)
            player = preloadedView.player as? ExoPlayer
            isSurfaceReady = preloadedView.width > 0 && preloadedView.height > 0
            Timber.tag(tag).d("Attached preloaded PlayerView for position $bindingAdapterPosition")
        }

        fun setupPlayerListener() {
            player?.addListener(object : Player.Listener {
                override fun onSurfaceSizeChanged(width: Int, height: Int) {
                    if (width > 0 && height > 0 && bindingAdapterPosition != RecyclerView.NO_POSITION) {
                        isSurfaceReady = true
                        Timber.tag(tag).d("Surface ready for position $bindingAdapterPosition: $width x $height")
                        playbackViewModel.updateSurfaceReadyState(position = bindingAdapterPosition, isReady = true)
                    }
                }
                override fun onPlaybackStateChanged(state: Int) {
                    Timber.tag(tag).d("Playback state changed for position $bindingAdapterPosition: $state")
                    when (state) {
                        Player.STATE_BUFFERING -> {
                            if (bindingAdapterPosition == playbackViewModel.getCurrentPlayingPosition()) {
                                showLoading()
                            }
                        }
                        Player.STATE_READY -> {
                            if (player?.isPlaying == true) {
                                hideLoading()
                            }
                        }
                        Player.STATE_ENDED, Player.STATE_IDLE -> {
                            hideLoading()
                        }
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