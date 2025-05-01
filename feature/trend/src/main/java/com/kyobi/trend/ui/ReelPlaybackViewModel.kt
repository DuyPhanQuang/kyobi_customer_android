package com.kyobi.trend.ui

import android.content.Context
import androidx.lifecycle.ViewModel
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
    private val pendingCallbacks = mutableMapOf<Int, MutableList<() -> Unit>>()
    var onRefreshSurface: ((position: Int) -> Unit)? = null

    fun setPlayerView(position: Int, playerView: PlayerView) {
        playerViews[position] = playerView
        tryTriggerReady(position)
        Timber.tag(tag).d("Stored PlayerView for position $position")
    }

    fun getPlayerView(position: Int): PlayerView? {
        return playerViews[position]
    }

    fun setPreloadedMediaItem(position: Int, mediaItem: MediaItem?) {
        if (mediaItem == null) return
        mediaItems[position] = mediaItem
        tryTriggerReady(position)
    }

    fun setPreloadedMediaSource(position: Int, mediaSource: MediaSource?) {
        if (mediaSource == null) return
        mediaSources[position] = mediaSource
        tryTriggerReady(position)
    }

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        reels.clear()
        reels.addAll(newReels)
    }

    fun getCurrentPlayingPosition(): Int = currentPlayingPosition

    fun updateSurfaceReadyState(position: Int, isReady: Boolean) {
        surfaceReadyStates[position] = isReady
        tryTriggerReady(position)
        Timber.tag(tag).d("Surface ready state updated for position $position: $isReady")
    }

    fun isReady(position: Int): Boolean {
        return mediaItems[position] != null &&
                mediaSources[position] != null &&
                playerViews[position] != null &&
                surfaceReadyStates[position] == true
    }

    private fun tryTriggerReady(position: Int) {
        if (isReady(position)) {
            pendingCallbacks.remove(position)?.forEach { it() }
        }
    }

    fun registerWhenReady(position: Int, callback: () -> Unit) {
        if (isReady(position)) {
            callback()
        } else {
            pendingCallbacks.getOrPut(position) { mutableListOf() }.add(callback)
        }
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

    fun startPlayerPlay(player: ExoPlayer, playerView: PlayerView) {
        if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
            player.prepare()
            Timber.tag(tag).d("Prepare player")
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
            pendingCallbacks.remove(position)
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