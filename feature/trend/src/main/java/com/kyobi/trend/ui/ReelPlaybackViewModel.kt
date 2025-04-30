package com.kyobi.trend.ui

import android.content.Context
import android.widget.ImageView
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
import androidx.media3.common.C
import androidx.media3.common.PlaybackException
import androidx.media3.datasource.DataSpec
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.PlayerView
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.kyobi.feature.trend.R

@UnstableApi
@HiltViewModel
class ReelPlaybackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: MediaCache,
    private val networkMonitor: NetworkMonitor,
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private var currentPlayingPosition: Int = -1
    private val surfaceReadyStates = mutableMapOf<Int, Boolean>() // Quản lý trạng thái surface
    private val reels = mutableListOf<Reel>()
    private val playLock = ReentrantLock()
    private val preparedMediaItems = mutableMapOf<Int, MediaItem>()
    private val playerPool = mutableListOf<ExoPlayer>() // Pool chứa các ExoPlayer tái sử dụng
    private val playerPoolSize = 3 // Giới hạn pool là 3 player
    private val usedPlayers = mutableMapOf<Int, ExoPlayer>() // Map theo dõi position -> ExoPlayer đang sử dụng
    private val positionsToKeepRange = 2 // Preload 5 video (position - 2 đến position + 2)
    private val playerViews = mutableMapOf<Int, PlayerView>() // Thêm map để lưu PlayerView
    private val preloadedMediaSourcePositions = mutableSetOf<Int>() // Thêm để theo dõi các position đã preload MediaSource

    init {
        // Lắng nghe trạng thái mạng để preload lại khi network restored
        viewModelScope.launch {
            networkMonitor.isConnected.collectLatest { isConnected ->
                if (isConnected && currentPlayingPosition >= 0) {
                    Timber.tag(tag).d("Network restored, preloading around position $currentPlayingPosition")
                    preloadMediaItemsAndMediaSource(currentPlayingPosition)
                    checkAndReplayVideoAfterNetworkRestored()
                }
            }
        }
        // Khởi tạo pool với 3 ExoPlayer
        repeat(playerPoolSize) {
            playerPool.add(ExoPlayer.Builder(context).build())
        }
    }

    fun setPlayerView(position: Int, playerView: PlayerView) {
        playerViews[position] = playerView
        Timber.tag(tag).d("Stored PlayerView for position $position")
    }

    fun removePlayerView(position: Int) {
        playerViews.remove(position)
        Timber.tag(tag).d("Removed PlayerView for position $position")
    }

    fun getOrCreatePlayerForPosition(position: Int): ExoPlayer {
        playLock.lock()
        try {
            // Kiểm tra xem position đã có player trong usedPlayers chưa
            if (usedPlayers.containsKey(position)) {
                Timber.tag(tag).d("Reusing existing player for position $position")
                return usedPlayers[position]!!
            }
            // Lấy player từ pool
            val selectedPlayer = if (playerPool.isNotEmpty()) {
                playerPool.removeAt(0)
            } else {
                // Nếu pool rỗng, lấy player từ vị trí xa nhất
                val farthestPos = usedPlayers.keys
                    .filter { it != currentPlayingPosition }
                    .maxByOrNull { kotlin.math.abs(it - position) }
                if (farthestPos != null) {
                    val farthestPlayer = usedPlayers.remove(farthestPos)
                    surfaceReadyStates.remove(farthestPos)
                    farthestPlayer?.let {
                        viewModelScope.launch {
                            withContext(Dispatchers.Default) {
                                if (it.isPlaying || it.playbackState == Player.STATE_READY || it.playbackState == Player.STATE_BUFFERING) {
                                    it.repeatMode = Player.REPEAT_MODE_OFF
                                    it.volume = 0f
                                    it.stop()
                                    it.clearMediaItems()
                                }
                                it.release() // Release player để đảm bảo không giữ trạng thái cũ
                                Timber.tag(tag).d("Released player at position $farthestPos before reuse")
                            }
                        }
                        ExoPlayer.Builder(context).build() // Tạo player mới thay vì tái sử dụng
                    } ?: ExoPlayer.Builder(context).build()
                } else {
                    ExoPlayer.Builder(context).build()
                }
            }
            // Reset player trước khi tái sử dụng
            viewModelScope.launch {
                withContext(Dispatchers.Default) {
                    selectedPlayer.clearMediaItems()
                    selectedPlayer.setPlaybackSpeed(1f)
                    selectedPlayer.volume = 0f
                    selectedPlayer.repeatMode = Player.REPEAT_MODE_OFF
                    selectedPlayer.stop()
                    Timber.tag(tag).d("Reset player for position $position")
                }
            }
            usedPlayers[position] = selectedPlayer
            Timber.tag(tag).d("Assigned new player to position $position, usedPlayers size: ${usedPlayers.size}")
            return selectedPlayer
        } finally {
            playLock.unlock()
        }
    }

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        reels.clear()
        reels.addAll(newReels)
        preparedMediaItems.clear()
        preloadedMediaSourcePositions.clear()
        // Tạo MediaItem cho các reel ngay cả khi không có mạng
        viewModelScope.launch {
            for (index in 0 until minOf(reels.size, positionsToKeepRange * 2 + 2)) {
                if (!preparedMediaItems.containsKey(index)) {
                    val reel = reels[index]
                    val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                        .setMediaId(reel.videoUrl).build()
                    preparedMediaItems[index] = mediaItem
                    Timber.tag(tag).d("Created MediaItem for position $index (no preload due to network state)")
                }
            }
        }
    }

    // Kiểm tra và phát lại video hiện tại nếu nó đang dừng hoặc lỗi do mạng hoặc player
    private fun checkAndReplayVideoAfterNetworkRestored() {
        playLock.lock()
        try {
            usedPlayers[currentPlayingPosition]?.let { player ->
                val playerView = playerViews[currentPlayingPosition]
                if (playerView != null) {
                    Timber.tag(tag).d("Network restored, attempting to replay video at position $currentPlayingPosition")
                    playVideoAtPositionInternal(position = currentPlayingPosition, playerView = playerView, player = player)
                } else {
                    Timber.tag(tag).w("No PlayerView found for position $currentPlayingPosition after network restore")
                }
            } ?: run {
                Timber.tag(tag).w("No player found for position $currentPlayingPosition after network restore")
            }
        } finally {
            playLock.unlock()
        }
    }

    fun getCurrentPlayingPosition(): Int = currentPlayingPosition

    fun updateSurfaceReadyState(position: Int, isReady: Boolean) {
        Timber.tag(tag).d("Surface ready state updated for position $position: $isReady")
        surfaceReadyStates[position] = isReady
    }

    private fun startCreateMediaSource(mediaItem: MediaItem): MediaSource {
        val cache = mediaCache.getCache()
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setFlags(0)
        val uri = mediaItem.localConfiguration?.uri?.toString() ?: ""
        return if (uri.endsWith(".m3u8", ignoreCase = true)) {
            // Nếu là HLS, dùng HlsMediaSource
            Timber.tag(tag).d("Creating HlsMediaSource for URI: $uri")
            HlsMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(mediaItem)
        } else {
            Timber.tag(tag).d("Creating ProgressiveMediaSource for URI: $uri")
            ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(mediaItem)
        }
    }

    private fun preloadVideoDataIntoCache(mediaSource: MediaSource, index: Int) {
        Timber.tag(tag).d("Starting preload for position $index")
        try {
            val cache = mediaCache.getCache()
            val dataSourceFactory = DefaultDataSource.Factory(context)
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(cache)
                .setUpstreamDataSourceFactory(dataSourceFactory)
                .setFlags(0)
            // Tạo một CacheDataSource để preload dữ liệu
            val cacheDataSource = cacheDataSourceFactory.createDataSource()
            // Tải trước một phần dữ liệu video (ví dụ: 5MB đầu tiên)
            val uri = mediaSource.mediaItem.localConfiguration?.uri
            if (uri != null) {
                // Tải 2 giây đầu tiên (giả sử bitrate trung bình là 1Mbps = 125KB/s)
                val bytesToLoad = 125 * 1024 * 2 // 2 giây = 250KB
                val dataSpec = DataSpec(uri, 0, bytesToLoad.toLong())
                cacheDataSource.open(dataSpec)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                var totalBytesRead = 0L
                while (cacheDataSource.read(buffer, 0, buffer.size).also { bytesRead = it } != C.RESULT_END_OF_INPUT) {
                    totalBytesRead += bytesRead
                    if (totalBytesRead >= bytesToLoad) break // Dừng khi tải đủ 2 giây
                }
                cacheDataSource.close()
                Timber.tag(tag).d("Preloaded 2 seconds of video data into cache for position $index")
            } else {
                Timber.tag(tag).w("Cannot preload data for position $index: Invalid URI")
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to preload data for position $index: ${e.message}")
        }
    }

    // Thực hiện 1. chuẩn bị MediaItem cho các position trong phạm vi
    // Thực hiện 2. chuẩn bị MediaSource cho 3 video tiếp theo kể từ position trên background thread
    // (vd: position = 0 thì chuẩn bị 1,2,3 -> preloadedDataPositions = 0,1,2,3,
    // position = 1 thì chuẩn bị 2,3,4 -> preloadedDataPositions = 0,1,2,3,4)
    private fun preloadMediaItemsAndMediaSource(position: Int) {
        if (!networkMonitor.isConnected.value) {
            Timber.tag(tag).w("No network connection, skipping preload for positions around $position")
            return
        }
        val start = maxOf(0, position - positionsToKeepRange)
        val end = minOf(reels.size - 1, position + positionsToKeepRange)
        val bufferEnd = minOf(reels.size - 1, position + 3) // Preload 3 video tiếp theo

        Timber.tag(tag).d("Preloading media items from $start to $end, buffer from $position to $bufferEnd")
        Timber.tag(tag).d("Current preloadedMediaSourcePositions: $preloadedMediaSourcePositions")

        viewModelScope.launch {
            // Bước 1: Chuẩn bị MediaItem cho các position trong phạm vi
            for (index in start..bufferEnd) {
                if (!preparedMediaItems.containsKey(index)) {
                    try {
                        val reel = reels[index]
                        val mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                            .setMediaId(reel.videoUrl).build()
                        preparedMediaItems[index] = mediaItem
                        Timber.tag(tag).d("Prepared MediaItem for position $index")
                    } catch (e: Exception) {
                        Timber.tag(tag).e(e, "Failed to prepare MediaItem for position $index")
                    }
                } else {
                    Timber.tag(tag).d("MediaItem already exists for position $index")
                }
            }
            // Bước 2: Preload dữ liệu cho 3 video tiếp theo trên background thread
            Timber.tag(tag).d("Starting preload loop from $position to $bufferEnd")
            if (position <= bufferEnd) {
                withContext(Dispatchers.IO) { // Chuyển sang Dispatchers.IO để thực hiện network operation
                    for (index in position..bufferEnd) {
                        Timber.tag(tag).d("Checking position $index")
                        if (!preparedMediaItems.containsKey(index)) {
                            Timber.tag(tag).w("Cannot preload data for position $index: MediaItem not prepared")
                            continue
                        }
                        if (index in preloadedMediaSourcePositions) {
                            Timber.tag(tag).d("Skipped preload for position $index: already preloaded")
                            continue
                        }
                        Timber.tag(tag).d("Preloading data for position $index")
                        val mediaItem = preparedMediaItems[index]!!
                        val mediaSource = startCreateMediaSource(mediaItem)
                        preloadVideoDataIntoCache(mediaSource, index)
                        preloadedMediaSourcePositions.add(index)
                        Timber.tag(tag).d("Added position $index to preloadedMediaSourcePositions")
                    }
                }
            } else {
                Timber.tag(tag).w("Invalid range for preload: position $position > bufferEnd $bufferEnd")
            }
        }
    }

    private fun safelyReleasePlayer(player: ExoPlayer, position: Int, type: String) {
        try {
            // Kiểm tra xem player có còn ở trạng thái có thể release không
            if (player.playbackState != Player.STATE_IDLE || player.isPlaying || player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING) {
                if (player.isPlaying || player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING) {
                    player.repeatMode = Player.REPEAT_MODE_OFF
                    player.stop()
                    player.clearMediaItems()
                    Timber.tag(tag).d("Stopped player at position $position ($type) before release")
                }
                player.release()
                Timber.tag(tag).d("Successfully released player at position $position ($type)")
            } else {
                Timber.tag(tag).d("Player at position $position ($type) already released or in idle state, skipping release")
            }
        } catch (e: Exception) {
            Timber.tag(tag).w(e, "Failed to release player at position $position ($type), marking for retry")
        }
    }

    private fun managePlayersAroundPosition(position: Int) {
        val start = maxOf(0, position - positionsToKeepRange)
        val end = minOf(reels.size - 1, position + positionsToKeepRange)
        Timber.tag(tag).d("Managing players: keeping players from position $start to $end")
        viewModelScope.launch {
            playLock.lock()
            try {
                val iterator = usedPlayers.iterator()
                while (iterator.hasNext()) {
                    val (pos, player) = iterator.next()
                    if (pos < start || pos > end) {
                        withContext(Dispatchers.Default) { // Chuyển sang background thread
                            if (player.isPlaying || player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING) {
                                player.repeatMode = Player.REPEAT_MODE_OFF
                                player.volume = 0f
                                player.stop()
                                player.clearMediaItems()
                                Timber.tag(tag).d("Stopped player at position $pos before returning to pool")
                            }
                            playerPool.add(player)
                            if (playerPool.size > playerPoolSize) {
                                val oldestPlayer = playerPool.removeAt(0)
                                safelyReleasePlayer(oldestPlayer, pos, "pool")
                                Timber.tag(tag).d("Released oldest player from pool to maintain size")
                            }
                            withContext(Dispatchers.Main) { // Cập nhật UI trên main thread
                                iterator.remove()
                                surfaceReadyStates.remove(pos)
                                Timber.tag(tag).d("Returned player at position $pos to pool")
                            }
                        }
                    }
                }
                // Xóa surfaceReadyStates của các position không còn trong phạm vi
                surfaceReadyStates.keys.toList().forEach { pos ->
                    if (pos < start || pos > end) {
                        surfaceReadyStates.remove(pos)
                        Timber.tag(tag).d("Removed surface ready state for position $pos")
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
            // Dừng các player khác
            usedPlayers.forEach { (pos, p) ->
                if (pos != position && (p.isPlaying || p.playbackState == Player.STATE_READY)) {
                    viewModelScope.launch(Dispatchers.Main) {
                        p.repeatMode = Player.REPEAT_MODE_OFF
                        p.pause()
                        p.volume = 0f
                        Timber.tag(tag).d("Paused player at position: $pos")
                    }
                }
            }
            // Sử dụng player được truyền vào từ ReelAdapter (đã được lấy từ pool)
            usedPlayers[position] = player
            Timber.tag(tag).d("usedPlayers: $usedPlayers")
            surfaceReadyStates[position] = isSurfaceReady
            Timber.tag(tag).d("Assigned player to position $position, usedPlayers size: ${usedPlayers.size}")
            currentPlayingPosition = position
            // Gọi preload cho position mới
            preloadMediaItemsAndMediaSource(position)
            managePlayersAroundPosition(position)
            // Luôn cố gắng phát video, ngay cả khi surface chưa sẵn sàng
            val playerView = playerViews[position]
            if (playerView != null) {
                playVideoAtPositionInternal(position = position, playerView = playerView, player = player)
                Timber.tag(tag).d("Attempting to play video at position $position, isSurfaceReady: $isSurfaceReady")
            } else {
                Timber.tag(tag).w("No PlayerView found for position $position")
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Error in playVideoAtPosition for position $position: ${e.message}")
        } finally {
            playLock.unlock()
        }
    }

    private fun playVideoAtPositionInternal(position: Int, playerView: PlayerView, player: ExoPlayer) {
        var mediaItem = preparedMediaItems[position]
        // Nếu MediaItem chưa được preload, thay vì báo lỗi tạo MediaItem ngay
        if (mediaItem == null) {
            Timber.tag(tag).w("MediaItem not preloaded for position $position, creating now")
            val reel = reels[position]
            mediaItem = MediaItem.fromUri(reel.videoUrl.toUri()).buildUpon()
                .setMediaId(reel.videoUrl).build()
            preparedMediaItems[position] = mediaItem
        }
        Timber.tag(tag).d("preparedMediaItems: $preparedMediaItems")
        // Bước 1: Tắt UI loading mặc định của PlayerView
        playerView.useController = false
        // Tìm ImageView thumbnail_view trong layout của PlayerView
        val thumbnailView = playerView.findViewById<ImageView>(R.id.thumbnail_view)
        viewModelScope.launch(Dispatchers.Main) {
            try {
                player.clearMediaItems()
                val mediaSource = startCreateMediaSource(mediaItem)
                player.setMediaSource(mediaSource)
                player.prepare()
                Timber.tag(tag).d("Prepared ExoPlayer for position $position")
                player.addListener(object : Player.Listener {
                    override fun onPlayerError(error: PlaybackException) {
                        Timber.tag(tag).e(error, "Playback error at position $position: ${error.message}")
                        if (error.message?.contains("Unexpected start code prefix") == true) {
                            // Retry phát lại video nếu gặp lỗi PesReader
                            viewModelScope.launch(Dispatchers.Main) {
                                player.clearMediaItems()
                                val newMediaSource = startCreateMediaSource(mediaItem)
                                player.setMediaSource(newMediaSource)
                                player.volume = 1f
                                player.repeatMode = Player.REPEAT_MODE_ONE
                                player.prepare()
                                player.play()
                                playerView.requestLayout()
                                playerView.invalidate()
                                Timber.tag(tag).d("Retried playing video at position $position after PesReader error")
                            }
                        }
                    }
                })
                player.volume = 1f
                player.repeatMode = Player.REPEAT_MODE_ONE
                if (!player.isPlaying) {
                    player.play()
                    Timber.tag(tag).d("Playing video at position $position, state: ${player.playbackState}, isPlaying: ${player.isPlaying}")
                    playerView.requestLayout()
                    playerView.invalidate()
                    Timber.tag(tag).d("Requesting PlayerView to invalidate for position $position")
                } else {
                    Timber.tag(tag).d("Video at position $position is already playing, skipping play")
                }
            } catch (e: IllegalStateException) {
                Timber.tag(tag).e(e, "Failed to play video at position $position: ${e.message}")
            }
        }
    }

    fun onPlayerReleased(position: Int) {
        playLock.lock()
        try {
            usedPlayers[position]?.let { player ->
                viewModelScope.launch(Dispatchers.Main) {
                    if (player.isPlaying || player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING) {
                        player.repeatMode = Player.REPEAT_MODE_OFF
                        player.volume = 0f
                        player.stop()
                        player.clearMediaItems()
                        Timber.tag(tag).d("Stopped player at position $position before returning to pool")
                    }
                    playerPool.add(player)
                    if (playerPool.size > playerPoolSize) {
                        val oldestPlayer = playerPool.removeAt(0)
                        safelyReleasePlayer(oldestPlayer, position, "pool")
                        Timber.tag(tag).d("Released oldest player from pool to maintain size")
                    }
                    Timber.tag(tag).d("Returned player at position $position to pool")
                }
            }
            usedPlayers.remove(position)
            surfaceReadyStates.remove(position)
            Timber.tag(tag).d("Player at position $position removed from usedPlayers after release in onViewRecycled")
        } finally {
            playLock.unlock()
        }
    }

    override fun onCleared() {
        super.onCleared()
        playLock.lock()
        try {
            usedPlayers.forEach { (pos, player) ->
                viewModelScope.launch(Dispatchers.Main) {
                    safelyReleasePlayer(player, pos, "active")
                }
            }
            usedPlayers.clear()
            playerPool.forEachIndexed { index, player ->
                viewModelScope.launch(Dispatchers.Main) {
                    safelyReleasePlayer(player, index, "pool")
                }
            }
            playerPool.clear()
            surfaceReadyStates.clear()
            preparedMediaItems.clear()
            preloadedMediaSourcePositions.clear()
        } finally {
            playLock.unlock()
        }
    }
}