package com.kyobi.trend.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import com.kyobi.trend.cache.MediaCache
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import java.util.concurrent.locks.ReentrantLock
import javax.inject.Inject
import androidx.core.net.toUri
import androidx.lifecycle.viewModelScope
import androidx.media3.common.PlaybackException
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@UnstableApi
@HiltViewModel
class ReelPlaybackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: MediaCache,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private var currentPlayingPosition: Int = -1
    private val activePlayers = mutableMapOf<Int, ExoPlayer>()
    private val surfaceReadyStates = mutableMapOf<Int, Boolean>() // Quản lý trạng thái surface
    private val reels = mutableListOf<Reel>()
    private val playLock = ReentrantLock()
    private val preparedMediaItems = mutableMapOf<Int, MediaItem>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val positionsToKeepRange = 2

    init {
        // Lắng nghe trạng thái mạng để preload lại khi network restored
        viewModelScope.launch {
            networkMonitor.isConnected.collectLatest { isConnected ->
                if (isConnected && currentPlayingPosition >= 0) {
                    Timber.tag(tag).d("Network restored, preloading around position $currentPlayingPosition")
                    preloadMediaItemsAroundPosition(currentPlayingPosition)
                    checkAndReplayVideoAfterNetworkRestored()
                }
            }
        }
    }

    // Kiểm tra và phát lại video hiện tại nếu nó đang dừng hoặc lỗi
    private fun checkAndReplayVideoAfterNetworkRestored() {
        playLock.lock()
        try {
            activePlayers[currentPlayingPosition]?.let { player ->
                val isSurfaceReady = surfaceReadyStates[currentPlayingPosition] ?: false
                if (isSurfaceReady && (player.playbackState == Player.STATE_ENDED || player.playbackState == Player.STATE_IDLE)) {
                    Timber.tag(tag).d("Network restored, attempting to replay video at position $currentPlayingPosition")
                    playVideoAtPositionInternal(currentPlayingPosition, player)
                }
            }
        } finally {
            playLock.unlock()
        }
    }

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        reels.clear()
        reels.addAll(newReels)
        // Preload video tại position 0 và các position lân cận
        if (reels.isNotEmpty()) {
            preloadMediaItemsAroundPosition(0)
        }
    }

    fun getCurrentPlayingPosition(): Int = currentPlayingPosition

    fun updateSurfaceReadyState(position: Int, isReady: Boolean) {
        playLock.lock()
        try {
            Timber.tag(tag).d("Surface ready state updated for position $position: $isReady")
            surfaceReadyStates[position] = isReady
            if (isReady && position == currentPlayingPosition) {
                activePlayers[position]?.let { player ->
                    if (!player.isPlaying) {
                        Timber.tag(tag).d("Starting play video at position $position")
                        playVideoAtPositionInternal(position, player)
                    } else {
                        Timber.tag(tag).d("Video at position $position is already playing, skipping play")
                    }
                } ?: run {
                    Timber.tag(tag).w("No player found for position $position")
                }
            }
        } finally {
            playLock.unlock()
        }
    }

    private fun preloadMediaItemsAroundPosition(position: Int) {
        if (!networkMonitor.isConnected.value) {
            Timber.tag(tag).w("No network connection, skipping preload for positions around $position")
            return
        }
        val start = maxOf(0, position - positionsToKeepRange)
        val end = minOf(reels.size - 1, position + positionsToKeepRange)
        Timber.tag(tag).d("Preloading media items from position $start to $end")
        for (index in start..end) {
            if (preparedMediaItems.containsKey(index)) {
                Timber.tag(tag).d("MediaItem already preloaded for position $index, skipping")
                continue
            }
            try {
                val reel = reels[index]
                val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                    .setMediaId(reel.videoUrl).build()
                preparedMediaItems[index] = mediaItem
                Timber.tag(tag).d("Preloaded MediaItem for position $index")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to preload MediaItem for position $index: ${e.message}")
            }
        }
    }

    private fun managePlayersAroundPosition(position: Int) {
        val start = maxOf(0, position - positionsToKeepRange)
        val end = minOf(reels.size - 1, position + positionsToKeepRange)
        Timber.tag(tag).d("Managing players: keeping players from position $start to $end")
        // thực hiện release các player ngoài keep range trên background thread
        coroutineScope.launch {
            playLock.lock()
            try {
                val iterator = activePlayers.iterator()
                while (iterator.hasNext()) {
                    val (pos, player) = iterator.next()
                    if (pos < start || pos > end) {
                        if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                            player.pause()
                            player.volume = 0f
                            Timber.tag(tag).d("Paused player at position $pos before release")
                        }
                        player.release()
                        iterator.remove()
                        surfaceReadyStates.remove(pos)
                        Timber.tag(tag).d("Released player at position $pos")
                    }
                }
            } finally {
                playLock.unlock()
            }
        }
    }

    fun playVideoAtPosition(position: Int, player: ExoPlayer, isSurfaceReady: Boolean) {
        Timber.tag(tag).d("playVideoAtPosition called for position $position, reels size: ${reels.size}")
        if (position < 0 || position >= reels.size) {
            Timber.tag(tag).d("Invalid position: $position, skipping playVideoAtPosition")
            return
        }
        playLock.lock()
        try {
            // Dừng tất cả player khác
            activePlayers.forEach { (pos, p) ->
                if (pos != position && (p.isPlaying || p.playbackState == Player.STATE_READY)) {
                    p.pause()
                    p.volume = 0f
                    Timber.tag(tag).d("Paused player at position: $pos")
                }
            }
            activePlayers[position] = player
            // Cập nhật trạng thái surface ban đầu (nếu chưa có)
            if (!surfaceReadyStates.containsKey(position)) {
                surfaceReadyStates[position] = isSurfaceReady
            }
            Timber.tag(tag).d("Added player to activePlayers at position $position, activePlayers size: ${activePlayers.size}")
            currentPlayingPosition = position
            preloadMediaItemsAroundPosition(position)  // Preload các video lân cận
            managePlayersAroundPosition(position)    // Quản lý player: giữ player trong phạm vi, giải phóng player ở xa
            val isSurfaceCurrentlyReady = surfaceReadyStates[position] ?: false  // Kiểm tra trạng thái surface từ surfaceReadyStates
            if (isSurfaceCurrentlyReady) {
                playVideoAtPositionInternal(position, player)
            } else {
                Timber.tag(tag).d("Surface not ready at position $position, waiting for surface ready event")
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error in playVideoAtPosition for position $position: ${e.message}")
        } finally {
            playLock.unlock()
        }
    }

    private fun playVideoAtPositionInternal(position: Int, player: ExoPlayer) {
        val mediaItem = preparedMediaItems[position]
        if (mediaItem != null) {
            if (!player.isPlaying || player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) {
                player.clearMediaItems() // Reset player nếu ở trạng thái lỗi hoặc kết thúc
                player.setMediaItem(mediaItem)
                player.prepare()
                Timber.tag(tag).d("Prepared ExoPlayer for position $position")
            }
            player.addListener(object : Player.Listener {
                override fun onPlayerError(error: PlaybackException) {
                    Timber.tag(tag).e(error, "Playback error at position $position: ${error.message}")
                    // TODO: Thông báo cho người dùng (ví dụ: hiển thị Toast hoặc UI lỗi)
                }
            })
            player.volume = 1f
            player.repeatMode = Player.REPEAT_MODE_ONE
            if (!player.isPlaying) {
                player.play()
                Timber.tag(tag).d("Playing video at position $position, state: ${player.playbackState}, isPlaying: ${player.isPlaying}")
            } else {
                Timber.tag(tag).d("Video at position $position is already playing, skipping play")
            }
        } else {
            Timber.tag(tag).e("MediaItem not preloaded for position $position")
            // TODO: Thông báo cho người dùng (ví dụ: hiển thị Toast hoặc UI lỗi)
        }
    }

    override fun onCleared() {
        super.onCleared()
        playLock.lock()
        try {
            activePlayers.forEach { (_, player) ->
                player.release()
            }
            activePlayers.clear()
            surfaceReadyStates.clear()
            preparedMediaItems.clear()
        } finally {
            playLock.unlock()
        }
    }
}