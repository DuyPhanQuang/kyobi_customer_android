package com.kyobi.trend.ui

import android.content.Context
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.kyobi.trend.cache.MediaCache
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import androidx.media3.common.PlaybackException
import androidx.media3.common.VideoSize
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.kyobi.trend.model.Reel
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class ReelPlaybackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: MediaCache,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private var currentPlayingPosition: Int = 0
    private val surfaceReadyStates = mutableMapOf<Int, Boolean>()
    private val reels = mutableListOf<Reel>()
    private val mediaItems = mutableMapOf<Int, MediaItem>()
    private val playerViews = mutableMapOf<Int, PlayerView>()
    private val mediaSources = mutableMapOf<Int, MediaSource>()
    var onRefreshSurface: ((position: Int) -> Unit)? = null

    fun setPlayerView(position: Int, playerView: PlayerView) {
        playerViews[position] = playerView
        Timber.tag(tag).d("Stored PlayerView for position $position")
    }

    fun getPlayerView(position: Int): PlayerView? {
        return playerViews[position]
    }

    fun setPreloadedMediaItem(position: Int, mediaItem: MediaItem?) {
        if (mediaItem == null) return
        mediaItems[position] = mediaItem
    }

    fun setPreloadedMediaSource(position: Int, mediaSource: MediaSource?) {
        if (mediaSource == null) return
        mediaSources[position] = mediaSource
    }

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        reels.clear()
        reels.addAll(newReels)
    }

    fun getCurrentPlayingPosition(): Int = currentPlayingPosition

    fun updateSurfaceReadyState(position: Int, isReady: Boolean) {
        Timber.tag(tag).d("Surface ready state updated for position $position: $isReady")
        surfaceReadyStates[position] = isReady
    }

    fun startCreateMediaSource(mediaItem: MediaItem): MediaSource? {
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

    fun onPageSelected(position: Int) {
        if (position < 0 || position >= reels.size) {
            Timber.tag(tag).d("Invalid position: $position, skipping onPageSelected")
            return
        }
        playerViews.forEach { (pos, playerView) ->
            if (pos != position) {
                val player = playerView.player as? ExoPlayer
                player?.let {
                    it.pause()
                    Timber.tag(tag).d("Paused media items at position $pos")
                    setMediaItemAndSourceForAnotherPlayer(pos, it)
                }
            }
        }
        val newPlayerView = playerViews[position]
        if (newPlayerView != null) {
            surfaceReadyStates[position] = newPlayerView.width > 0 && newPlayerView.height > 0
            Timber.tag(tag).d("Checking size of PlayerView at position: $position: ${newPlayerView.width}x${newPlayerView.height}")
            val newPlayer = newPlayerView.player as? ExoPlayer
            newPlayer?.let { p ->
                playVideoAtPositionInternal(position, newPlayerView, p)
            }
        } else {
            Timber.tag(tag).w("No PlayerView found for position $position")
        }
        currentPlayingPosition = position
        // Start player prepare next position
        val nextPosition = position + 1
        startPlayerPrepareAtNextPosition(nextPosition)
    }

    fun createDrawMeasureVideoAtPosition(position: Int, isSurfaceReady: Boolean) {
        Timber.tag(tag).d("createDrawMeasureVideoAtPosition called for position $position, reels size: ${reels.size}")
        if (position < 0 || position >= reels.size) {
            Timber.tag(tag).d("Invalid position: $position, skipping createDrawMeasureVideoAtPosition")
            return
        }
        val playerView = playerViews[position] ?: return
        try {
            val player = playerView.player as ExoPlayer
            surfaceReadyStates[position] = isSurfaceReady
            warmupMediaItemMediaSourceAtPosition(position, player)
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
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error in createDrawMeasureVideoAtPosition for position $position: ${e.message}")
        }
    }

    private fun startPlayerPrepareAtNextPosition(nextPosition: Int) {
        val playerView = playerViews[nextPosition] ?: return
        val player = playerView.player as? ExoPlayer ?: return
        val isSurfaceReady = playerView.width > 0 && playerView.height > 0
        if (!isSurfaceReady) return
        if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
            player.setPriority(C.PRIORITY_PLAYBACK_PRELOAD)
            player.prepare()
            player.playWhenReady = false
            Timber.tag(tag).d("Prepared player for keyframe at nextPosition: $nextPosition")
        }
    }

    private fun setMediaItemAndSourceForAnotherPlayer(pos: Int, anotherPlayer: ExoPlayer) {
        val mediaItem = mediaItems[pos] ?: return
        val mediaSource = mediaSources[pos] ?: return
        anotherPlayer.setMediaItem(mediaItem)
        anotherPlayer.setMediaSource(mediaSource)
    }

    // tải trước MediaItem và MediaSource cho các position
    private fun warmupMediaItemMediaSourceAtPosition(
        position: Int,
        player: ExoPlayer
    ) {
        if (position >= 0 && position < reels.size) {
            val preloadedMediaItem = mediaItems[position]
            val preloadedMediaSource = mediaSources[position]
            if (preloadedMediaItem != null && preloadedMediaSource != null) {
                mediaItems[position] = preloadedMediaItem
                player.setMediaItem(preloadedMediaItem)
                mediaSources[position] = preloadedMediaSource
                player.setMediaSource(preloadedMediaSource)
                Timber.tag(tag).d("Using preload data to preloaded mediaItem mediaSource for position $position")
            }
        }
    }

    private fun startPlayerPlay(player: ExoPlayer, playerView: PlayerView) {
        if (currentPlayingPosition == 0) {
            player.prepare()
            Timber.tag(tag).d("Prepare first player (index = 0)")
        }
        player.setPriority(C.PRIORITY_PLAYBACK)
        player.volume = 1f
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.play()
        playerView.requestLayout()
        playerView.post { playerView.invalidate() }
        onRefreshSurface?.invoke(currentPlayingPosition)
    }

    private fun playVideoAtPositionInternal(position: Int, playerView: PlayerView, player: ExoPlayer) {
        if (surfaceReadyStates[position] == false) {
            Timber.tag(tag).w("Surface at position $position not ready")
            return
        }
        try {
            startPlayerPlay(player, playerView)
            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Timber.tag(tag).e(error, "Playback error at position $position: ${error.message}")
                    if (error.message?.contains("Unexpected start code prefix") == true) {
                        startPlayerPlay(player, playerView)
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
                    surfaceReadyStates[position] = width > 0 && height > 0
                }
            })
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
        playerViews[position]?.let { playerView ->
            val player = playerView.player as ExoPlayer
            startPlayerEnd(position, player)
            surfaceReadyStates.remove(position)
            mediaItems.remove(position)
            mediaSources.remove(position)
            playerViews.remove(position)?.player = null
        }
    }

    override fun onCleared() {
        super.onCleared()
        playerViews.forEach { (position, playerView) ->
            playerView.player = null
            Timber.tag(tag).d("Cleared PlayerView at position $position")
        }
        playerViews.clear()
        surfaceReadyStates.clear()
        mediaItems.clear()
        mediaSources.clear()
    }
}