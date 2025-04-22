package com.kyobi.trend.ui

import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ConcatenatingMediaSource2
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.feature.trend.R
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.config.ReelConfig
import com.kyobi.trend.config.ReelConfigViewModel
import com.kyobi.trend.model.Reel
import com.kyobi.featurecommon.monitor.network.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import timber.log.Timber
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@UnstableApi
class ReelAdapter(
    val reels: List<Reel>,
    private val context: Context,
    private val mediaCache: MediaCache,
    private val lifecycleOwner: LifecycleOwner,
    private val configViewModel: ReelConfigViewModel,
    private val networkMonitor: NetworkMonitor,
    private val recyclerView: RecyclerView,
) : RecyclerView.Adapter<ReelAdapter.ReelViewHolder>() {
    private var lastPlayTime = 0L
    private val playDebounceDuration = 300L
    private var lastPreloadTime = 0L
    private val preloadDebounceDuration = 300L
    private var lastCleanupTime = 0L
    private val cleanupDebounceDuration = 500L
    var currentPlayingPosition: Int = RecyclerView.NO_POSITION // Theo dõi position đang phát
        private set
    private val mediaSources = mutableMapOf<Int, MediaSource>() // Lưu MediaSource đã preload
    private val playLock = ReentrantLock() // Lock để đồng bộ playVideoAtPosition
    private val activePlayers = mutableMapOf<Int, ExoPlayer>() // Lưu các player đang hoạt động
    private val downloadedFiles = mutableMapOf<Int, File>() // Lưu file đã tải
    private val downloadLatches = mutableMapOf<Int, CountDownLatch>() // Lưu trạng thái tải file
    private val mainHandler = Handler(Looper.getMainLooper()) // Thread để switch luồng phát
    private var config: ReelConfig = ReelConfig()

    init {
        lifecycleOwner.lifecycleScope.launch {
            configViewModel.config.collectLatest { newConfig ->
                config = newConfig
                Timber.tag("ReelAdapter").d("Updated config: $config")
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReelViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reel, parent, false)
        val displayMetrics = parent.context.resources.displayMetrics
        view.layoutParams = RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, displayMetrics.heightPixels)
        return ReelViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReelViewHolder, position: Int) {
        // reset trạng thái
        holder.player?.let { player ->
            if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                player.stop()
                player.clearMediaItems()
                player.repeatMode = Player.REPEAT_MODE_OFF
                Timber.tag("ReelAdapter").d("Paused player at position $position during onBindViewHolder")
            }
            if (position != currentPlayingPosition) {
                activePlayers.remove(position)
            }
        }
        holder.bind(reels[position], position)
    }

    override fun getItemCount(): Int = reels.size

    override fun onViewRecycled(holder: ReelViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.bindingAdapterPosition
        if (position != RecyclerView.NO_POSITION) {
            val positionsToKeep = positionsToKeep(currentPlayingPosition)
            holder.player?.let { player ->
                if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                    if (position in positionsToKeep) {
                        player.repeatMode = Player.REPEAT_MODE_OFF
                        player.pause()
                        Timber.tag("ReelAdapter").d("Paused player at position $position during onViewRecycled (in positionsToKeep)")
                    } else {
                        player.repeatMode = Player.REPEAT_MODE_OFF
                        player.stop()
                        player.clearMediaItems()
                        Timber.tag("ReelAdapter").d("Stopped and cleared player at position $position during onViewRecycled (not in positionsToKeep)")
                        activePlayers.remove(position)
                        CoroutineScope(Dispatchers.IO).launch {
                            holder.releasePlayer()
                        }
                    }
                }
            }
        }
    }

    fun playVideoAtPosition(position: Int) {
        if (position < 0 || position >= reels.size) {
            Timber.tag("ReelAdapter").d("Invalid position: $position, skipping playVideoAtPosition")
            return
        }
        if (position == currentPlayingPosition) {
            val currentPlayer = activePlayers[position]
            if (currentPlayer != null && currentPlayer.isPlaying) {
                Timber.tag("ReelAdapter").d("Player at position $position is already playing, skipping")
                return
            }
        }
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPlayTime < playDebounceDuration) {
            Timber.tag("ReelAdapter").d("Skipped play at position $position due to debounce")
            return
        }
        playLock.lock()
        try {
            lastPlayTime = currentTime
            Timber.tag("ReelAdapter").d("Before pausing other players, activePlayers: ${activePlayers.keys}")
            val positionsToKeep = positionsToKeep(position)
            activePlayers.forEach { (pos, player) ->
                if (pos != position) {
                    if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                        if (pos in positionsToKeep) {
                            player.repeatMode = Player.REPEAT_MODE_OFF
                            player.pause()
                            Timber.tag("ReelAdapter").d("Paused player at position: $pos (in positionsToKeep)")
                        } else {
                            player.repeatMode = Player.REPEAT_MODE_OFF
                            player.stop()
                            player.clearMediaItems()
                            Timber.tag("ReelAdapter").d("Stopped and cleared player at position: $pos (not in positionsToKeep)")
                        }
                    }
                }
            }
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val holder = recyclerView.getChildViewHolder(child) as? ReelViewHolder
                holder?.player?.let { player ->
                    val pos = holder.bindingAdapterPosition
                    if (pos != position && pos != RecyclerView.NO_POSITION) {
                        if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                            if (pos in positionsToKeep) {
                                player.repeatMode = Player.REPEAT_MODE_OFF
                                player.pause()
                                Timber.tag("ReelAdapter").d("Paused player at position: $pos in RecyclerView (in positionsToKeep)")
                            } else {
                                player.repeatMode = Player.REPEAT_MODE_OFF
                                player.stop()
                                player.clearMediaItems()
                                Timber.tag("ReelAdapter").d("Stopped and cleared player at position: $pos in RecyclerView (not in positionsToKeep)")
                            }
                        }
                    }
                }
            }
            val maxDistance = 10
            activePlayers.keys.toList().forEach { pos ->
                if (pos != position && (pos < position - maxDistance || pos > position + maxDistance)) {
                    val player = activePlayers[pos]
                    player?.stop()
                    player?.clearMediaItems()
                    player?.repeatMode = Player.REPEAT_MODE_OFF
                    activePlayers.remove(pos)
                    Timber.tag("ReelAdapter").d("Removed player at position: $pos from activePlayers (outside max distance)")
                }
            }
            val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelViewHolder
            if (holder != null) {
                holder.player?.let { player ->
                    activePlayers[position] = player
                    Timber.tag("ReelAdapter").d("Added player to activePlayers at position $position, activePlayers size: ${activePlayers.size}")

                    if (player.playbackState == Player.STATE_ENDED ||
                        player.playbackState == Player.STATE_IDLE ||
                        (player.playbackState == Player.STATE_READY && !player.isPlaying)) {
                        player.repeatMode = Player.REPEAT_MODE_OFF
                        player.stop()
                        player.clearMediaItems()

                        // Tạo ConcatenatingMediaSource2
                        val mediaSources = mutableListOf<MediaSource>()
                        val remoteMediaSource = createMediaSource(position, useLocal = false)
                        mediaSources.add(remoteMediaSource)

                        // Nếu file local đã tồn tại, thêm vào danh sách
                        if (downloadedFiles.containsKey(position)) {
                            val localMediaSource = createMediaSource(position, useLocal = true)
                            mediaSources.add(localMediaSource)
                        }

                        val builder = ConcatenatingMediaSource2.Builder()
                        mediaSources.forEach { mediaSource ->
                            builder.add(mediaSource, 3000)
                        }
                        val concatenatingMediaSource = builder.build()
                        player.setMediaSource(concatenatingMediaSource)
                    }

                    Timber.tag("ReelAdapter").d("Before prepare - Player state at position $position: ${player.playbackState}, isPlaying: ${player.isPlaying}")
                    player.volume = 1f
                    player.repeatMode = Player.REPEAT_MODE_ONE
                    player.prepare()
                    player.play()
                    Timber.tag("ReelAdapter").d("After prepare - Player state at position $position: ${player.playbackState}, isPlaying: ${player.isPlaying}")

                    // Kiểm tra và chuyển nguồn nếu file local đã tồn tại hoặc sau khi tải xong
                    val switchToLocalIfAvailable = {
                        Timber.tag("ReelAdapter").d("Checking downloadedFiles for position $position: containsKey=${downloadedFiles.containsKey(position)}, filePath=${downloadedFiles[position]?.path}")
                        val playbackState = player.playbackState
                        if (downloadedFiles.containsKey(position) &&
                            (downloadedFiles[position]?.length() ?: 0L) >= 1024 &&
                            (playbackState == Player.STATE_BUFFERING ||
                                    playbackState == Player.STATE_READY ||
                                    playbackState == Player.STATE_ENDED ||
                                    playbackState == Player.STATE_IDLE)
                        ) {
                            val currentUri = player.currentMediaItem?.mediaId
                            val localUri = downloadedFiles[position]?.path
                            if (currentUri != localUri) {
                                val localMediaSource = createMediaSource(position, useLocal = true)
                                val concatenatingMediaSource = ConcatenatingMediaSource2.Builder()
                                    .add(localMediaSource, 3000)
                                    .build()
                                val currentPositionMs = player.currentPosition
                                val wasPlaying = player.isPlaying // Lưu trạng thái isPlaying
                                player.setMediaSource(concatenatingMediaSource)
                                player.seekTo(currentPositionMs)
                                player.prepare()
                                if (wasPlaying) {
                                    player.play() // Tự động phát lại nếu video đang phát trước đó
                                }
                                Timber.tag("ReelAdapter").d("Switched to local file at position: $position localFile: ${downloadedFiles[position]?.path}")
                            }
                        } else {
                            Timber.tag("ReelAdapter").d("Did not switch to local file at position $position: containsKey=${downloadedFiles.containsKey(position)}, playbackState=$playbackState")
                            if (downloadedFiles.containsKey(position)) {
                                mainHandler.postDelayed({
                                    if (playbackState != Player.STATE_ENDED && position == currentPlayingPosition) {
                                        Timber.tag("ReelAdapter").d("Retrying switch to local file at position: $position")
                                        val retryPlaybackState = player.playbackState
                                        if (downloadedFiles.containsKey(position) &&
                                            (downloadedFiles[position]?.length() ?: 0L) >= 1024 &&
                                            (retryPlaybackState == Player.STATE_BUFFERING ||
                                                    retryPlaybackState == Player.STATE_READY ||
                                                    retryPlaybackState == Player.STATE_ENDED ||
                                                    retryPlaybackState == Player.STATE_IDLE)
                                        ) {
                                            val currentUri = player.currentMediaItem?.mediaId
                                            val localUri = downloadedFiles[position]?.path
                                            if (currentUri != localUri) {
                                                val localMediaSource = createMediaSource(position, useLocal = true)
                                                val concatenatingMediaSource = ConcatenatingMediaSource2.Builder()
                                                    .add(localMediaSource, 3000)
                                                    .build()
                                                val currentPositionMs = player.currentPosition
                                                val wasPlaying = player.isPlaying // Lưu trạng thái isPlaying
                                                player.setMediaSource(concatenatingMediaSource)
                                                player.seekTo(currentPositionMs)
                                                player.prepare()
                                                if (wasPlaying) {
                                                    player.play() // Tự động phát lại nếu video đang phát trước đó
                                                }
                                                Timber.tag("ReelAdapter").d("Switched to local file after retry at position: $position localFile: ${downloadedFiles[position]?.path}")
                                            }
                                        }
                                    }
                                }, 500)
                            }
                        }
                    }
                    switchToLocalIfAvailable()
                    if (!downloadedFiles.containsKey(position) && !downloadLatches.containsKey(position)) {
                        downloadVideoPartial(position) {
                            mainHandler.post {
                                switchToLocalIfAvailable()
                            }
                        }
                    }
                }
            } else {
                Timber.tag("ReelAdapter").w("Holder not found at position: $position")
            }
            currentPlayingPosition = position

            CoroutineScope(Dispatchers.IO).launch {
                cleanupOldFiles(currentPlayingPosition)
            }
            val layoutManager = recyclerView.layoutManager as? LinearLayoutManager
            val firstVisible = layoutManager?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
            val lastVisible = layoutManager?.findLastVisibleItemPosition() ?: RecyclerView.NO_POSITION
            if (firstVisible != RecyclerView.NO_POSITION && lastVisible != RecyclerView.NO_POSITION) {
                preloadVideos(firstVisible, lastVisible)
            }
        } catch (e: Exception) {
            Timber.tag("ReelAdapter").e(e, "Error in playVideoAtPosition for position $position ${e.message}")
        } finally {
            playLock.unlock()
        }
    }

    fun preloadVideos(firstVisiblePosition: Int, lastVisiblePosition: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPreloadTime < preloadDebounceDuration) {
            return
        }
        lastPreloadTime = currentTime

        val preloadCount = 2
        val preloadStart = (currentPlayingPosition - preloadCount).coerceAtLeast(0)
        val preloadEnd = (currentPlayingPosition + preloadCount).coerceAtMost(reels.size - 1)

        for (pos in preloadStart..preloadEnd) {
            if (pos !in firstVisiblePosition..lastVisiblePosition) {
                if (!mediaSources.containsKey(pos)) {
                    val mediaSource = createMediaSource(pos)
                    mediaSources[pos] = mediaSource
                    Timber.tag("ReelAdapter").d("Preloaded MediaSource for position $pos")
                }
                // Tải file thủ công nếu chưa tải
                if (!downloadedFiles.containsKey(pos) && !downloadLatches.containsKey(pos)) {
                    Timber.tag("ReelAdapter").d("Starting preload for position $pos in onScrolled")
                    downloadVideoPartial(pos)
                }
            }
        }

        val positionsToKeep = positionsToKeep(currentPlayingPosition)
        mediaSources.keys.toList().forEach { pos ->
            if (pos !in positionsToKeep) {
                mediaSources.remove(pos)
                Timber.tag("ReelAdapter").d("Cleared MediaSource for position $pos in onScrolled")
            } else {
                Timber.tag("ReelAdapter").d("Kept MediaSource for position $pos in onScrolled")
            }
        }
        downloadLatches.keys.toList().forEach { pos ->
            if (pos !in positionsToKeep) {
                downloadLatches.remove(pos)?.countDown()
                Timber.tag("ReelAdapter").d("Canceled preload for position: $pos in onScrolled")
            } else {
                Timber.tag("ReelAdapter").d("Kept download latch for position: $pos in onScrolled")
            }
        }
        CoroutineScope(Dispatchers.IO).launch {
            cleanupOldFiles(currentPlayingPosition)
        }
    }

    private fun createMediaSource(position: Int, useLocal: Boolean = false): MediaSource {
        val remoteUri = Uri.parse(reels[position].videoUrl)
        val localFile = downloadedFiles[position]

        return if (useLocal && localFile != null) {
            Timber.tag("ReelAdapter").d("Creating local media source for position: $position")
            val mediaItem = MediaItem.fromUri(localFile.path).buildUpon().setMediaId(localFile.path).build()
            ProgressiveMediaSource.Factory(DefaultDataSource.Factory(context)).createMediaSource(mediaItem)
        } else {
            Timber.tag("ReelAdapter").d("Creating remote media source for position: $position")
            val cacheDataSourceFactory = CacheDataSource.Factory()
                .setCache(mediaCache.getCache())
                .setUpstreamDataSourceFactory(DefaultHttpDataSource.Factory())
                .setCacheWriteDataSinkFactory(CacheDataSink.Factory().setCache(mediaCache.getCache()))
            val mediaItem = MediaItem.fromUri(remoteUri).buildUpon().setMediaId(remoteUri.toString()).build()
            ProgressiveMediaSource.Factory(cacheDataSourceFactory).createMediaSource(mediaItem)
        }
    }

    private fun downloadVideoPartial(position: Int, onComplete: () -> Unit = {}) {
        if (position < 0 || position >= reels.size) return

        val maxBytes = config.downloadSizeMb * 1024 * 1024L
        synchronized(downloadedFiles) {
            if (downloadedFiles.containsKey(position)) {
                Timber.tag("ReelAdapter").d("First ${config.downloadSizeMb}MB of video at position $position already downloaded")
                return
            }
        }
        synchronized(downloadLatches) {
            if (downloadLatches.containsKey(position)) {
                Timber.tag("ReelAdapter").d("Download already in progress for position $position, skipping")
                return
            }
        }

        if (!networkMonitor.isConnected.value) {
            Timber.tag("ReelAdapter").w("No network available, skipping download for position $position")
            return
        }

        val latch = CountDownLatch(1)
        synchronized(downloadLatches) {
            downloadLatches[position] = latch
        }
        val url = reels[position].videoUrl
        val file = File(recyclerView.context.cacheDir, "video_${position}_partial.mp4")
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=0-${maxBytes - 1}")
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.let { body ->
                        val contentLength = body.contentLength()
                        Timber.tag("ReelAdapter").d("Content length for position $position: $contentLength bytes")
                        if (contentLength <= 0) {
                            Timber.tag("ReelAdapter").w("Content length is 0 or invalid for position $position")
                            return@let
                        }
                        val sink = file.sink().buffer()
                        var bytesWritten = 0L
                        val source = body.source()
                        while (!source.exhausted() && bytesWritten < maxBytes) {
                            val bytesRead = source.read(sink.buffer, (maxBytes - bytesWritten).coerceAtMost(8192L))
                            if (bytesRead == -1L) break
                            bytesWritten += bytesRead
                            sink.emit()
                        }
                        sink.close()
                        val bytesDownloaded = file.length()
                        if (bytesDownloaded != bytesWritten) {
                            Timber.tag("ReelAdapter").w("Mismatch in downloaded bytes for position $position: expected $bytesWritten, got $bytesDownloaded")
                        }
                        if (bytesDownloaded < 1024) {
                            Timber.tag("ReelAdapter").w("Downloaded file too small for position $position: $bytesDownloaded bytes, skipping")
                            file.delete()
                            return@let
                        }
                        synchronized(downloadedFiles) {
                            downloadedFiles[position] = file
                        }
                        Timber.tag("ReelAdapter").d("Successfully downloaded first ${config.downloadSizeMb}MB for position $position ($bytesDownloaded bytes)")
                        onComplete()
                    } ?: run {
                        Timber.tag("ReelAdapter").w("Failed to download first ${config.downloadSizeMb}MB for position $position: Empty response body")
                    }
                } else {
                    Timber.tag("ReelAdapter").w("Failed to download first ${maxBytes}MB for position $position: HTTP ${response.code}")
                }
            } catch (e: Exception) {
                Timber.tag("ReelAdapter").e(e, "Error downloading first ${maxBytes}MB for position $position")
            } finally {
                latch.countDown()
                synchronized(downloadLatches) {
                    downloadLatches.remove(position)
                }
            }
        }
    }

    // khoanh vùng vị trí hợp lệ tải trước
    private fun positionsToKeep(currentPosition: Int): Set<Int> {
        val range = (currentPosition - config.positionsToKeepRange..currentPosition + config.positionsToKeepRange)
            .filter { it >= 0 && it < reels.size }
            .toSet()
        return range
    }

    private fun cleanupOldFiles(currentPosition: Int) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastCleanupTime < cleanupDebounceDuration) {
            Timber.tag("ReelAdapter").d("Skipped cleanupOldFiles due to debounce")
            return
        }
        lastCleanupTime = currentTime

        val positionsToKeep = positionsToKeep(currentPosition)
        synchronized(downloadLatches) {
            downloadLatches.keys.toList().forEach { pos ->
                if (pos !in positionsToKeep) {
                    downloadLatches.remove(pos)?.countDown()
                    Timber.tag("ReelAdapter").d("Canceled download for position: $pos")
                }
            }
        }
        synchronized(downloadedFiles) {
            val iterator = downloadedFiles.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                val position = entry.key
                if (position !in positionsToKeep) {
                    val file = entry.value
                    file.delete()
                    iterator.remove()
                    Timber.tag("ReelAdapter").d("Deleted old partial video file for position $position")
                }
            }
        }
    }

    fun releaseAllPlayers() {
        playLock.lock()
        try {
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val holder = recyclerView.getChildViewHolder(child) as? ReelViewHolder
                holder?.releasePlayer(true)
            }
            CoroutineScope(Dispatchers.IO).launch {
                downloadedFiles.values.forEach { it.delete() }
                downloadedFiles.clear()
                downloadLatches.clear()
                mediaCache.release()
            }
            mediaSources.clear()
            activePlayers.clear()
            Timber.tag("ReelAdapter").d("Cleared all downloaded partial files, media sources, and active players")
        } finally {
            playLock.unlock()
        }
    }

    fun retryDownloads() {
        if (!networkMonitor.isConnected.value) {
            Timber.tag("ReelAdapter").w("No network available, skipping retry downloads")
            return
        }
        // Xác định các position cần retry (trong phạm vi positionsToKeep)
        val positionsToKeep = positionsToKeep(currentPlayingPosition)
        val positionsToRetry = positionsToKeep.filter { position ->
            // Chỉ retry các position chưa tải và không đang tải
            position >= 0 && position < reels.size && !downloadedFiles.containsKey(position) && !downloadLatches.containsKey(position)
        }
        if (positionsToRetry.isEmpty()) {
            Timber.tag("ReelAdapter").d("No positions to retry downloading")
            return
        }
        Timber.tag("ReelAdapter").d("Retrying downloads for positions: $positionsToRetry")
        for (position in positionsToRetry) {
            // Tải file và chuyển nguồn nếu cần
            downloadVideoPartial(position) {
                // Chuyển callback sang main thread
                mainHandler.post {
                    // Callback để chuyển nguồn phát sang file local nếu position đang phát
                    if (position == currentPlayingPosition) {
                        val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelViewHolder
                        holder?.player?.let { player ->
                            val playbackState = player.playbackState
                            if (downloadedFiles.containsKey(position) &&
                                (playbackState == Player.STATE_BUFFERING ||
                                        playbackState == Player.STATE_READY ||
                                        playbackState == Player.STATE_ENDED ||
                                        playbackState == Player.STATE_IDLE)) {
                                val currentUri = player.currentMediaItem?.mediaId
                                val localUri = downloadedFiles[position]?.path
                                if (currentUri != localUri) {
                                    val localMediaSource = createMediaSource(position, useLocal = true)
                                    val concatenatingMediaSource = ConcatenatingMediaSource2.Builder()
                                        .add(localMediaSource, 3000)
                                        .build()
                                    val currentPositionMs = player.currentPosition
                                    val wasPlaying = player.isPlaying
                                    player.setMediaSource(concatenatingMediaSource)
                                    player.seekTo(currentPositionMs)
                                    player.prepare()
                                    if (wasPlaying) {
                                        player.play()
                                    }
                                    Timber.tag("ReelAdapter").d("Switched to local file after retry at position: $position localFile: ${downloadedFiles[position]?.path}")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun configPlayerView(playerView: PlayerView) {
        playerView.setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
        playerView.setBackgroundColor(Color.TRANSPARENT)
        playerView.setKeepContentOnPlayerReset(true)
        playerView.setUseController(false)
        playerView.setResizeMode(RESIZE_MODE_FILL)
    }

    inner class ReelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val playerView: PlayerView = itemView.findViewById(R.id.player_view)
        private val tvReelInfo: TextView = itemView.findViewById(R.id.tv_reel_info)
        var player: ExoPlayer? = null
        private var retryCount = 0
        private val maxRetries = 3

        init {
            player = ExoPlayer.Builder(context)
                .setLoadControl(
                    DefaultLoadControl.Builder()
                        .setBufferDurationsMs(
                            config.bufferMinMs,
                            config.bufferMaxMs,
                            config.bufferPlaybackMs,
                            config.bufferRebufferMs
                        )
                        .build()
                )
                .build()
                .apply {
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(state: Int) {
                            Timber.tag("ReelAdapter").d("Player state at position $bindingAdapterPosition: $state")
                        }
                        override fun onPlayerError(error: PlaybackException) {
                            Timber.tag("ReelAdapter").e("Error playing video at position $bindingAdapterPosition: ${error.message}")

                            if (retryCount < maxRetries && bindingAdapterPosition == currentPlayingPosition) {
                                if (bindingAdapterPosition < 0 || bindingAdapterPosition >= reels.size) {
                                    Timber.tag("ReelAdapter").w("Invalid position $bindingAdapterPosition during retry, skipping")
                                    return
                                }
                                retryCount++
                                Timber.tag("ReelAdapter").d("Retrying playback at position $bindingAdapterPosition, attempt $retryCount")
                                postDelayed({
                                    CoroutineScope(Dispatchers.Default).launch {
                                        if (bindingAdapterPosition < 0 || bindingAdapterPosition >= reels.size) {
                                            Timber.tag("ReelAdapter").w("Invalid position $bindingAdapterPosition during retry callback, skipping")
                                            return@launch
                                        }
                                        val remoteUri = Uri.parse(reels[bindingAdapterPosition].videoUrl)
                                        val mediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                                            .createMediaSource(MediaItem.fromUri(remoteUri))
                                        withContext(Dispatchers.Main) {
                                            Timber.tag("ReelAdapter").w("Started switching to main thread")
                                            setMediaSource(mediaSource)
                                            volume = 1f
                                            repeatMode = Player.REPEAT_MODE_ONE
                                            playWhenReady = true
                                            prepare()
                                            play()
                                        }
                                    }
                                }, 500)
                            } else {
                                retryCount = 0
                                Timber.tag("ReelAdapter").w("Max retries reached at position $bindingAdapterPosition, skipping")
                                Toast.makeText(context, "Error playing video at position $bindingAdapterPosition", Toast.LENGTH_SHORT).show()
                            }
                        }
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            Timber.tag("ReelAdapter").d("Is playing at position $bindingAdapterPosition: $isPlaying")
                            // Reset retry count khi phát thành công
                            if (isPlaying) retryCount = 0
                        }
                    })
                }
            playerView.player = player
            configPlayerView(playerView)
        }

        fun bind(reel: Reel, position: Int) {
            Timber.tag("ReelAdapter").d("Binding position: $position, player exists: ${player != null}")
            // Đảm bảo player không phát ngầm khi bind, nhưng không reset trạng thái
            player?.let {
                if (it.isPlaying || it.playbackState == Player.STATE_READY) {
                    it.pause()
                    it.repeatMode = Player.REPEAT_MODE_OFF
                    Timber.tag("ReelAdapter").d("Paused player at position $position during bind")
                }
            }
            tvReelInfo.text = """
                ID: ${reel.id}
                Likes: ${reel.likeCount}
                Comments: ${reel.commentCount}
                Views: ${reel.viewCount}
                Tags: ${reel.tags?.joinToString() ?: "None"}
            """.trimIndent()
        }

        fun releasePlayer(force: Boolean = false) {
            if (!force && bindingAdapterPosition != RecyclerView.NO_POSITION) {
                activePlayers.remove(bindingAdapterPosition)
                Timber.tag("ReelAdapter").d("Removed player from activePlayers at position $bindingAdapterPosition, activePlayers size: ${activePlayers.size}")
            }
            playerView.removeCallbacks(null)
            player?.release() // Chỉ release khi cần, không đặt player = null nếu ko force
            // force = true khi recyclerView releases
            if (force) {
                activePlayers.remove(bindingAdapterPosition)
                player = null
                playerView.player = null
            }
            Timber.tag("ReelAdapter").d("Released player at position $bindingAdapterPosition")
        }

        private fun postDelayed(action: () -> Unit, delayMs: Long) {
            playerView.postDelayed(action, delayMs)
        }
    }
}