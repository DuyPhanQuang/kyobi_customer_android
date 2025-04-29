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
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private val preloadPlayers = mutableMapOf<Int, ExoPlayer>() // Quản lý các ExoPlayer dùng để preload

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

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        reels.clear()
        reels.addAll(newReels)
        // Tạo MediaItem cho các reel ngay cả khi không có mạng
        coroutineScope.launch {
            for (index in 0 until minOf(reels.size, positionsToKeepRange * 2 + 1)) {
                if (!preparedMediaItems.containsKey(index)) {
                    val reel = reels[index]
                    val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                        .setMediaId(reel.videoUrl).build()
                    preparedMediaItems[index] = mediaItem
                    Timber.tag(tag).d("Created MediaItem for position $index (no preload due to network state)")
                }
            }
        }
        // Preload video tại position 0 và các position lân cận
        if (reels.isNotEmpty()) {
            preloadMediaItemsAroundPosition(0)
        }
    }

    // Kiểm tra và phát lại video hiện tại nếu nó đang dừng hoặc lỗi do mạng hoặc player
    private fun checkAndReplayVideoAfterNetworkRestored() {
        playLock.lock()
        try {
            activePlayers[currentPlayingPosition]?.let { player ->
                Timber.tag(tag).d("Network restored, attempting to replay video at position $currentPlayingPosition")
                playVideoAtPositionInternal(currentPlayingPosition, player)
            } ?: run {
                Timber.tag(tag).w("No player found for position $currentPlayingPosition after network restore")
            }
        } finally {
            playLock.unlock()
        }
    }

    fun getCurrentPlayingPosition(): Int = currentPlayingPosition

    fun updateSurfaceReadyState(position: Int, isReady: Boolean) {
        playLock.lock()
        try {
            Timber.tag(tag).d("Surface ready state updated for position $position: $isReady")
            surfaceReadyStates[position] = isReady
            if (isReady) {
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

    private fun startCreateMediaSource(mediaItem: MediaItem): MediaSource {
        val cache = mediaCache.getCache()
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)
        val mediaSource: MediaSource = ProgressiveMediaSource.Factory(cacheDataSourceFactory)
            .createMediaSource(mediaItem)
        return mediaSource
    }

    // Tải trước dữ liệu video vào cache. thực hiện trên main thread
    private fun preloadVideoDataIntoCache(mediaSource: MediaSource, index: Int) {
        val preloadPlayer = ExoPlayer.Builder(context).build()
        preloadPlayer.setMediaSource(mediaSource)
        preloadPlayer.prepare()
        preloadPlayers[index] = preloadPlayer
    }

    private fun preloadMediaItemsAroundPosition(position: Int) {
        if (!networkMonitor.isConnected.value) {
            Timber.tag(tag).w("No network connection, skipping preload for positions around $position")
            return
        }
        val start = maxOf(0, position - positionsToKeepRange)
        val end = minOf(reels.size - 1, position + positionsToKeepRange)
        Timber.tag(tag).d("Preloading media items from position $start to $end")
        // Chạy preload trên Background thread để tạo MediaItem và MediaSource
        coroutineScope.launch {
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
                    // Tạo MediaSource trên Background thread
                    val mediaSource = startCreateMediaSource(mediaItem)
                    // Chuyển sang Main thread để preload
                    withContext(Dispatchers.Main) {
                        preloadVideoDataIntoCache(mediaSource, index)
                    }
                    Timber.tag(tag).d("Preloaded MediaItem and data for position $index")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to preload MediaItem for position $index: ${e.message}")
                }
            }
        }
    }

    private fun managePlayersAroundPosition(position: Int) {
        val start = maxOf(0, position - positionsToKeepRange)
        val end = minOf(reels.size - 1, position + positionsToKeepRange)
        Timber.tag(tag).d("Managing players: keeping players from position $start to $end")
        // thực hiện trên background thread
        coroutineScope.launch {
            playLock.lock()
            try {
                // Release các active players ngoài phạm vi
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
                // Release các preload players ngoài phạm vi
                val preloadIterator = preloadPlayers.iterator()
                while (preloadIterator.hasNext()) {
                    val (pos, preloadPlayer) = preloadIterator.next()
                    if (pos < start || pos > end) {
                        preloadPlayer.release()
                        preloadIterator.remove()
                        Timber.tag(tag).d("Released preload player at position $pos")
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
            managePlayersAroundPosition(position)    // Quản lý player: giữ player trong phạm vi, release player ở xa
            // Phát video ngay lập tức, bỏ qua kiểm tra surface
            playVideoAtPositionInternal(position, player)
            if (!isSurfaceReady) {
                Timber.tag(tag).d("Surface not ready at position $position, but attempted to play anyway")
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
                val mediaSource = startCreateMediaSource(mediaItem)
                player.setMediaSource(mediaSource)
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
            preloadPlayers.forEach { (_, player) ->
                player.release()
            }
            preloadPlayers.clear()
        } finally {
            playLock.unlock()
        }
    }
}