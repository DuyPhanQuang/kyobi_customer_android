package com.kyobi.trend.ui

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.kyobi.trend.cache.MediaCache
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class ReelPlaybackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: MediaCache,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private var currentPlayingPosition: Int = -1
    private val surfaceReadyStates = mutableMapOf<Int, Boolean>()
    private val reels = mutableListOf<Reel>()
    private val preparedMediaItems = mutableMapOf<Int, MediaItem>()
    private val playerViews = mutableMapOf<Int, PlayerView>()
    private val players = mutableMapOf<Int, ExoPlayer>()
    var onRefreshSurface: ((position: Int) -> Unit)? = null

    fun setPlayerView(position: Int, playerView: PlayerView) {
        playerViews[position] = playerView
        Timber.tag(tag).d("Stored PlayerView for position $position")
    }

    fun getPlayerView(position: Int): PlayerView? {
        return playerViews[position]
    }

    fun getOrCreatePlayerForPosition(position: Int): ExoPlayer {
        return players.getOrPut(position) {
            ExoPlayer.Builder(context).build().also {
                Timber.tag(tag).d("Created new player for position $position")
            }
        }
    }

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        reels.clear()
        reels.addAll(newReels)
        preparedMediaItems.clear()
        viewModelScope.launch {
            for (index in newReels.indices) {
                val reel = newReels[index]
                val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                    .setMediaId(reel.videoUrl).build()
                preparedMediaItems[index] = mediaItem
                Timber.tag(tag).d("Created MediaItem for position $index")
            }
        }
    }

    fun getCurrentPlayingPosition(): Int = currentPlayingPosition

    fun updateSurfaceReadyState(position: Int, isReady: Boolean) {
        Timber.tag(tag).d("Surface ready state updated for position $position: $isReady")
        surfaceReadyStates[position] = isReady
    }

    private fun startCreateMediaSource(mediaItem: MediaItem): MediaSource? {
        try {
            val cache = mediaCache.getCache()
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(dataSourceFactory)
            val uri = mediaItem.localConfiguration?.uri?.toString() ?: ""
            if (uri.endsWith(".mp4")) {
                cacheDataSourceFactory.setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)
            } else {
                cacheDataSourceFactory.setFlags(0)
            }
            return if (uri.endsWith(".m3u8", ignoreCase = true)) {
                Timber.tag(tag).d("Creating HlsMediaSource for URI: $uri")
                HlsMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(mediaItem)
            } else {
                Timber.tag(tag).d("Creating ProgressiveMediaSource for URI: $uri")
                ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(mediaItem)
            }
        } catch (e: Exception) {
            Timber.tag(tag).d("Failed to startCreateMediaSource with mediaItem: $mediaItem error: ${e.message}")
            return null
        }
    }

    fun onPageSelected(position: Int, player: ExoPlayer) {
        if (position < 0 || position >= reels.size) {
            Timber.tag(tag).d("Invalid position: $position, skipping onPageSelected")
            return
        }
        if (position != currentPlayingPosition) {
            players[currentPlayingPosition]?.volume = 0f
            players[currentPlayingPosition]?.pause()
        }
        currentPlayingPosition = position
        val playerView = playerViews[position]
        if (playerView != null) {
            playVideoAtPositionInternal(position, playerView, player)
        } else {
            Timber.tag(tag).w("No PlayerView found for position $position")
        }
    }

    fun createDrawMeasureVideoAtPosition(position: Int, player: ExoPlayer, isSurfaceReady: Boolean) {
        Timber.tag(tag).d("createDrawMeasureVideoAtPosition called for position $position, reels size: ${reels.size}")
        if (position < 0 || position >= reels.size) {
            Timber.tag(tag).d("Invalid position: $position, skipping createDrawMeasureVideoAtPosition")
            return
        }
        try {
            surfaceReadyStates[position] = isSurfaceReady
            var playerView = playerViews[position]
            if (playerView == null) {
                playerView = PlayerView(context).apply {
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
                playerView.player = player
                setPlayerView(position, playerView)
                val widthSpec = View.MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.widthPixels, View.MeasureSpec.EXACTLY)
                val heightSpec = View.MeasureSpec.makeMeasureSpec(context.resources.displayMetrics.heightPixels, View.MeasureSpec.EXACTLY)
                playerView.measure(widthSpec, heightSpec)
                playerView.layout(0, 0, playerView.measuredWidth, playerView.measuredHeight)
                Timber.tag(tag).d("Created new PlayerView for position $position")
            }
            player.addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    Timber.tag(tag).d("createDrawMeasureVideoAtPosition - First frame rendered for position $position")
                    playerView.post { playerView.invalidate() }
                }
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    Timber.tag(tag).d("createDrawMeasureVideoAtPosition - Video size changed for position $position: ${videoSize.width}x${videoSize.height}")
                    playerView.requestLayout()
                    playerView.post { playerView.invalidate() }
                }
                override fun onSurfaceSizeChanged(width: Int, height: Int) {
                    Timber.tag(tag).d("createDrawMeasureVideoAtPosition - Surface size changed for position $position: ${width}x${height}")
                    surfaceReadyStates[position] = width > 0 && height > 0
                }
            })
            playerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                override fun onGlobalLayout() {
                    startPlayerPrepare(position, player)
                    Timber.tag(tag).d("createDrawMeasureVideoAtPosition - Delayed playback started for position $position")
                    playerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                }
            })
            Timber.tag(tag).d("Used PlayerView at position $position")
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error in createDrawMeasureVideoAtPosition for position $position: ${e.message}")
        }
    }

    private fun startPlayerPrepare(position: Int, player: ExoPlayer) {
        val mediaItem = preparedMediaItems[position]
        if (mediaItem != null) {
            val mediaSource = startCreateMediaSource(mediaItem)
            if (mediaSource != null) {
                player.setMediaSource(mediaSource)
                player.prepare()
                Timber.tag(tag).d("Prepared player for position $position")
            }
        } else {
            Timber.tag(tag).w("No MediaItem for position $position")
        }
    }

    private fun startPlayerPlay(player: ExoPlayer) {
        player.volume = 1f
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.play()
    }

    private fun playVideoAtPositionInternal(position: Int, playerView: PlayerView, player: ExoPlayer) {
        var mediaItem = preparedMediaItems[position]
        if (mediaItem == null) {
            Timber.tag(tag).w("MediaItem not preloaded for position $position, creating now")
            val reel = reels[position]
            mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                .setMediaId(reel.videoUrl).build()
            preparedMediaItems[position] = mediaItem
        }
        try {
            val mediaSource = startCreateMediaSource(mediaItem)
            if (mediaSource != null) {
                player.setMediaSource(mediaSource)
            }
            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Timber.tag(tag).e(error, "Playback error at position $position: ${error.message}")
                    if (error.message?.contains("Unexpected start code prefix") == true) {
                        player.clearMediaItems()
                        val newMediaSource = startCreateMediaSource(mediaItem)
                        if (newMediaSource != null) {
                            player.setMediaSource(newMediaSource)
                        }
                        startPlayerPlay(player)
                        playerView.requestLayout()
                        playerView.post { playerView.invalidate() }
                        onRefreshSurface?.invoke(position)
                        Timber.tag(tag).d("Retried playing video at position $position after PesReader error")
                    }
                }
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    Timber.tag(tag).d("IsPlaying changed for position $position: $isPlaying")
                    if (isPlaying) {
                        Timber.tag(tag).d("Video is actually playing at position $position")
                    } else {
                        Timber.tag(tag).w("Video is not playing at position $position, current state: ${player.playbackState}")
                    }
                }
                override fun onRenderedFirstFrame() {
                    Timber.tag(tag).d("First frame rendered for position $position")
                    playerView.post { playerView.invalidate() }
                }
                override fun onVideoSizeChanged(videoSize: VideoSize) {
                    Timber.tag(tag).d("Video size changed for position $position: ${videoSize.width}x${videoSize.height}")
                    playerView.requestLayout()
                    playerView.post { playerView.invalidate() }
                }
                override fun onSurfaceSizeChanged(width: Int, height: Int) {
                    Timber.tag(tag).d("Surface size changed for position $position: ${width}x${height}")
                    if (width > 0 && height > 0) {
                        surfaceReadyStates[position] = true
                    }
                }
            })
            if (surfaceReadyStates[position] == true) {
                startPlayerPlay(player)
                Timber.tag(tag).d("Playing video at position $position, state: ${player.playbackState}, isPlaying: ${player.isPlaying}")
            } else {
                Timber.tag(tag).d("Surface not ready for position $position, delaying playback")
                playerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
                    override fun onGlobalLayout() {
                        if (playerView.width > 0 && playerView.height > 0) {
                            surfaceReadyStates[position] = true
                            startPlayerPlay(player)
                            Timber.tag(tag).d("Delayed playback started for position $position")
                            playerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
                        }
                    }
                })
            }
            playerView.requestLayout()
            playerView.post { playerView.invalidate() }
            onRefreshSurface?.invoke(position)
        } catch (e: IllegalStateException) {
            Timber.tag(tag).e(e, "Failed to play video at position $position: ${e.message}")
        }
    }

    private fun startPlayerEnd(position: Int?, player: ExoPlayer) {
        player.repeatMode = Player.REPEAT_MODE_OFF
        player.volume = 0f
        player.pause()
        player.stop()
        player.clearMediaItems()
        player.release()
        Timber.tag(tag).d("start player end at position $position")
    }

    fun onPlayerReleased(position: Int) {
        players[position]?.let { player ->
            startPlayerEnd(position, player)
            players.remove(position)
            surfaceReadyStates.remove(position)
            playerViews.remove(position)?.player = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        players.forEach { (position, player) ->
            startPlayerEnd(position, player)
            Timber.tag(tag).d("Ended Player at position $position")
        }
        players.clear()
        playerViews.forEach { (position, playerView) ->
            playerView.player = null
            Timber.tag(tag).d("Cleared PlayerView at position $position")
        }
        playerViews.clear()
        surfaceReadyStates.clear()
        preparedMediaItems.clear()
    }
}