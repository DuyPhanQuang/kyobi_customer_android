package com.kyobi.trend.ui

import android.content.Context
import androidx.lifecycle.ViewModel
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
    private var currentPlayingPosition: Int = 0
    private val surfaceReadyStates = mutableMapOf<Int, Boolean>()
    private val reels = mutableListOf<Reel>()
    private val preparedMediaItems = mutableMapOf<Int, MediaItem>()
    private val playerViews = mutableMapOf<Int, PlayerView>()
    private val preloadedMediaSources = mutableMapOf<Int, MediaSource>()
    var onRefreshSurface: ((position: Int) -> Unit)? = null

    fun setPlayerView(position: Int, playerView: PlayerView) {
        playerViews[position] = playerView
        Timber.tag(tag).d("Stored PlayerView for position $position")
    }

    fun getPlayerView(position: Int): PlayerView? {
        return playerViews[position]
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
                    it.stop()
                    it.clearMediaItems()
                    Timber.tag(tag).d("Paused stopped player and cleared media items at position $pos")
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
            warmupPlayerNearbyPosition(position, player)
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

    fun warmupPlayerNearbyPositions(visiblePositions: List<Int>) {
        Timber.tag(tag).d("Preparing players for nearby positions: $visiblePositions")
        visiblePositions.forEach { position ->
            if (position >= 0 && position < reels.size) {
                val playerView = playerViews[position]
                val nearPlayer = playerView?.player as? ExoPlayer
                val mediaItem = preparedMediaItems[position]
                val mediaSource = preloadedMediaSources[position]
                if (nearPlayer != null && mediaItem != null && mediaSource != null) {
                    nearPlayer.clearMediaItems()
                    nearPlayer.setMediaItem(mediaItem)
                    nearPlayer.setMediaSource(mediaSource)
                    nearPlayer.prepare()
                    Timber.tag(tag).d("Prepared player with preloaded MediaItem MediaSource for keyframe at position $position")
                }
            }
        }
    }

    // tải trước MediaItem và MediaSource cho các position
    private fun warmupPlayerNearbyPosition(position: Int, player: ExoPlayer) {
        if (position >= 0 && position < reels.size && !preloadedMediaSources.containsKey(position)) {
            val reel = reels[position]
            val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                .setMediaId(reel.videoUrl).build()
            preparedMediaItems[position] = mediaItem
            Timber.tag(tag).d("Preloaded mediaItem for position $position")
            player.setMediaItem(mediaItem)
            val mediaSource = startCreateMediaSource(mediaItem) ?: return
            preloadedMediaSources[position] = mediaSource
            Timber.tag(tag).d("Preloaded mediaSource for position $position")
            player.setMediaSource(mediaSource)
        }
    }

    private fun startPlayerPlay(player: ExoPlayer, playerView: PlayerView) {
        if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
            player.prepare()
            Timber.tag(tag).d("Prepared player due to IDLE or ENDED state")
        }
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
            preloadedMediaSources.remove(position)
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
        preparedMediaItems.clear()
        preloadedMediaSources.clear()
    }
}