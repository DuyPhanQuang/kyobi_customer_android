package com.kyobi.trend

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.feature.trend.R
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import timber.log.Timber

@UnstableApi
class ReelAdapter(
    private val reels: List<Reel>,
    private val context: Context,
    private val mediaCache: MediaCache,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<ReelAdapter.ReelViewHolder>() {

    private var currentPlayer: ExoPlayer? = null
    private var currentPlayingPosition: Int = -1
    private var preloadPlayer: ExoPlayer? = null
    private var preloadPosition: Int = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reel, parent, false)
        val displayMetrics = parent.context.resources.displayMetrics
        view.layoutParams = RecyclerView.LayoutParams(
            RecyclerView.LayoutParams.MATCH_PARENT,
            displayMetrics.heightPixels
        )
        return ReelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        holder.bind(reels[position], position)
    }

    override fun getItemCount(): Int = reels.size

    override fun onViewRecycled(holder: ReelViewHolder) {
        super.onViewRecycled(holder)
        holder.releasePlayer()
    }

    fun playVideoAtPosition(position: Int) {
        Timber.tag("ReelAdapter")
            .d("Playing video at position: $position, currentPlayingPosition: $currentPlayingPosition")
        if (position != currentPlayingPosition && position >= 0 && position < reels.size) {
            // Release player cũ
            currentPlayer?.release()
            currentPlayer = null
            currentPlayingPosition = position

            // Tạo player mới
            val player = ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(
                        CacheDataSource.Factory()
                            .setCache(mediaCache.obtainCache())
                            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
                    )
                )
                .setLoadControl(
                    DefaultLoadControl.Builder()
                        .setBufferDurationsMs(5000, 15000, 2000, 2000)
                        .build()
                )
                .build()
                .apply {
                    setMediaItem(MediaItem.fromUri(reels[position].videoUrl))
                    playWhenReady = true
                    volume = 1f
                    prepare()
                    play()
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            Timber.tag("ReelAdapter").d("Player state at position $position: $state")
                            val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelViewHolder
                            holder?.itemView?.findViewById<ProgressBar>(R.id.progress_loading)?.visibility =
                                if (state == Player.STATE_BUFFERING) View.VISIBLE else View.GONE
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            Timber.tag("ReelAdapter").e("Error playing video at position $position: ${error.message}")
                            Toast.makeText(context, "Error playing video at position $position", Toast.LENGTH_SHORT).show()
                        }
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            Timber.tag("ReelAdapter").d("Is playing at position $position: $isPlaying")
                        }
                    })
                }
            currentPlayer = player

            // Cập nhật PlayerView
            val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelViewHolder
            if (holder != null) {
                Timber.tag("ReelAdapter").d("Holder found at position: $position, updating PlayerView")
                holder.playerView.player = player
                holder.player = player
            } else {
                Timber.tag("ReelAdapter").w("Holder not found at position: $position")
            }

            // Preload video tiếp theo
            preloadNextVideo(position)
        }
    }

    private fun preloadNextVideo(currentPosition: Int) {
        // Release preload player cũ nếu có
        preloadPlayer?.release()
        preloadPlayer = null
        preloadPosition = -1

        val nextPosition = currentPosition + 1
        if (nextPosition < reels.size) {
            Timber.tag("ReelAdapter").d("Preloading video at position: $nextPosition")
            val player = ExoPlayer.Builder(context)
                .setMediaSourceFactory(
                    DefaultMediaSourceFactory(
                        CacheDataSource.Factory()
                            .setCache(mediaCache.obtainCache())
                            .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
                    )
                )
                .setLoadControl(
                    DefaultLoadControl.Builder()
                        .setBufferDurationsMs(5000, 15000, 2000, 2000)
                        .build()
                )
                .build()
                .apply {
                    setMediaItem(MediaItem.fromUri(reels[nextPosition].videoUrl))
                    playWhenReady = false
                    volume = 0f
                    prepare()
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            Timber.tag("ReelAdapter").d("Preload player state at position $nextPosition: $state")
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            Timber.tag("ReelAdapter").e("Error preloading video at position $nextPosition: ${error.message}")
                            Toast.makeText(context, "Error preloading video at position $nextPosition", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
            preloadPlayer = player
            preloadPosition = nextPosition
        }
    }

    fun releaseAllPlayers() {
        currentPlayer?.release()
        currentPlayer = null
        preloadPlayer?.release()
        preloadPlayer = null
        currentPlayingPosition = -1
        preloadPosition = -1
    }

    inner class ReelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val playerView: PlayerView = itemView.findViewById(R.id.player_view)
        private val tvReelInfo: TextView = itemView.findViewById(R.id.tv_reel_info)
        var player: ExoPlayer? = null

        fun bind(reel: Reel, position: Int) {
            Timber.tag("ReelAdapter").d("Binding position: $position, player exists: ${player != null}")

            tvReelInfo.text = """
                ID: ${reel.id}
                Likes: ${reel.likeCount}
                Comments: ${reel.commentCount}
                Views: ${reel.viewCount}
                Tags: ${reel.tags?.joinToString() ?: "None"}
            """.trimIndent()

            // Reset playerView
            playerView.player = null
            player = null
        }

        fun releasePlayer() {
            // Không release player đang phát
            if (player == currentPlayer || player == preloadPlayer) {
                return
            }
            player?.release()
            player = null
        }
    }
}