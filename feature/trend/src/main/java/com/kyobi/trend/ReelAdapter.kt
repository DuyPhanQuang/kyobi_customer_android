package com.kyobi.trend

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
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.FileDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import androidx.media3.ui.AspectRatioFrameLayout.RESIZE_MODE_FILL
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.feature.trend.R
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CountDownLatch
import java.util.concurrent.locks.ReentrantLock

@UnstableApi
class ReelAdapter(
    private val reels: List<Reel>,
    private val context: Context,
    private val mediaCache: MediaCache,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<ReelAdapter.ReelViewHolder>() {
    private var currentPlayingPosition = -1 // Theo dõi position đang phát
    private val downloadedFiles = mutableMapOf<Int, File>() // Lưu file đã tải
    private val mediaSources = mutableMapOf<Int, MediaSource>() // Lưu MediaSource đã preload
    private val downloadLatches = mutableMapOf<Int, CountDownLatch>() // Lưu trạng thái tải file
    private val playLock = ReentrantLock() // Lock để đồng bộ playVideoAtPosition
    private val dataSourceFactories = mutableMapOf<Int, DataSource.Factory>()
    private val mainHandler = Handler(Looper.getMainLooper()) // Thread để switch luồng phát
    private val activePlayers = mutableMapOf<Int, ExoPlayer>() // Lưu các player đang hoạt động

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
        // Dừng và reset player trước khi bind để tránh phát ngầm
        holder.player?.let { player ->
            if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                player.stop()
                player.clearMediaItems()
                player.repeatMode = Player.REPEAT_MODE_OFF
                Timber.tag("ReelAdapter").d("Stopped player at position $position during onBindViewHolder")
            }
            activePlayers.remove(position)
        }
        holder.bind(reels[position], position)
    }

    override fun getItemCount(): Int = reels.size

    override fun onViewRecycled(holder: ReelViewHolder) {
        super.onViewRecycled(holder)
        holder.player?.let { player ->
            if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                player.stop()
                player.clearMediaItems()
                player.repeatMode = Player.REPEAT_MODE_OFF
                Timber.tag("ReelAdapter").d("Stopped player at position ${holder.bindingAdapterPosition} during onViewRecycled")
            }
            activePlayers.remove(holder.bindingAdapterPosition)
            Timber.tag("ReelAdapter").d("Removed player at position ${holder.bindingAdapterPosition} from activePlayers during onViewRecycled")
        }
        holder.releasePlayer()
    }

    fun playVideoAtPosition(position: Int) {
        // Kiểm tra điều kiện cơ bản
        if (position < 0 || position >= reels.size) {
            Timber.tag("ReelAdapter").d("Invalid position: $position, skipping playVideoAtPosition")
            return
        }

        // Kiểm tra nếu position hiện tại đang phát và không cần reset
        if (position == currentPlayingPosition) {
            val currentPlayer = activePlayers[position]
            if (currentPlayer != null && currentPlayer.isPlaying) {
                Timber.tag("ReelAdapter").d("Player at position $position is already playing, skipping")
                return
            }
        }

        // đảm bảo đồng bộ để tránh race condition khi scroll nhanh
        playLock.lock()

        try {
            // trạng thái activePlayers trước khi xử lý
            Timber.tag("ReelAdapter").d("Before pausing other players, activePlayers: ${activePlayers.keys}")

            // Dừng tất cả player trong activePlayers
            activePlayers.forEach { (pos, player) ->
                if (pos != position) {
                    if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                        player.stop()
                        player.clearMediaItems()
                        player.repeatMode = Player.REPEAT_MODE_OFF // Đặt lại repeat mode
                        Timber.tag("ReelAdapter").d("Stopped player at position: $pos")
                    }
                }
            }

            // Dừng tất cả player hiện tại trong RecyclerView (đề phòng trường hợp player chưa được thêm vào activePlayers)
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val holder = recyclerView.getChildViewHolder(child) as? ReelViewHolder
                holder?.player?.let { player ->
                    val pos = holder.bindingAdapterPosition
                    if (pos != position && pos != RecyclerView.NO_POSITION) {
                        if (player.isPlaying || player.playbackState == Player.STATE_READY) {
                            player.stop()
                            player.clearMediaItems()
                            player.repeatMode = Player.REPEAT_MODE_OFF
                            Timber.tag("ReelAdapter").d("Stopped player at position: $pos in RecyclerView")
                        }
                    }
                }
            }

            // Xóa sạch activePlayers trước khi thêm player mới
            activePlayers.clear()

            // Phát ExoPlayer tại position mới
            val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelViewHolder
            if (holder != null) {
                holder.player?.let { player ->
                    // Thêm player vào activePlayers khi phát
                    activePlayers[position] = player
                    Timber.tag("ReelAdapter").d("Added player to activePlayers at position $position, activePlayers size: ${activePlayers.size}")

                    // Reset player trước khi phát để tránh trạng thái cũ
                    player.stop()
                    player.clearMediaItems()

                    // Ban đầu luôn phát từ remote URL
                    val remoteMediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                        .createMediaSource(MediaItem.fromUri(reels[position].videoUrl))
                    player.setMediaSource(remoteMediaSource)
                    player.playWhenReady = true
                    player.volume = 1f
                    player.repeatMode = Player.REPEAT_MODE_OFF
                    player.prepare()
                    player.play()
                    Timber.tag("ReelAdapter").d("Played player at position: $position")

                    // Đảm bảo PlayerView hiển thị và surface được làm mới
                    holder.playerView.setVisibility(View.VISIBLE)

                    // Kiểm tra và chuyển nguồn nếu file local đã tồn tại hoặc sau khi tải xong
                    val switchToLocalIfAvailable = {
                        Timber.tag("ReelAdapter").d("Checking downloadedFiles for position $position: containsKey=${downloadedFiles.containsKey(position)}, filePath=${downloadedFiles[position]?.path}")
                        // Chuyển nguồn nếu player đang ở trạng thái BUFFERING hoặc READY
                        val playbackState = player.playbackState

                        if (downloadedFiles.containsKey(position) &&
                            (playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY)) {
                            setPlayVideoWhenSwitchedSource(player, holder.playerView, position)
                        } else {
                            Timber.tag("ReelAdapter").d("Did not switch to local file at position $position: containsKey=${downloadedFiles.containsKey(position)}, isPlaying=${player.isPlaying}")
                            // Kiểm tra lại sau x ms nếu chưa chuyển được
                            if (downloadedFiles.containsKey(position)) {
                                mainHandler.postDelayed({
                                    if (playbackState != Player.STATE_ENDED && position == currentPlayingPosition) {
                                        Timber.tag("ReelAdapter").d("Retrying switch to local file at position: $position")
                                        val retryPlaybackState = player.playbackState
                                        if (downloadedFiles.containsKey(position) && (retryPlaybackState == Player.STATE_BUFFERING || retryPlaybackState == Player.STATE_READY)) {
                                            setPlayVideoWhenSwitchedSource(player, holder.playerView, position)
                                        } else {
                                            Timber.tag("ReelAdapter").d("Retry failed to switch to local file at position $position: containsKey=${downloadedFiles.containsKey(position)}, retryPlaybackState=$retryPlaybackState")
                                        }
                                    }
                                }, 1000)
                            }
                        }
                    }

                    // Kiểm tra ngay lập tức nếu file đã tồn tại
                    switchToLocalIfAvailable()

                    // Tải song song nếu chưa có, và chuyển nguồn sau khi tải xong
                    if (!downloadedFiles.containsKey(position)) {
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

            // Tải trước video tiếp theo và preload MediaSource
            downloadNextVideoPartial(position)
        } catch (e: Exception) {
            Timber.tag("ReelAdapter").e(e, "Error in playVideoAtPosition for position $position ${e.message}")
        } finally {
            playLock.unlock()
        }
    }

    private fun createMediaSource(position: Int): MediaSource {
        val localFile = downloadedFiles[position]
        val remoteUri = Uri.parse(reels[position].videoUrl)

        if (localFile != null && localFile.exists()) {
            Timber.tag("ReelAdapter").d("Using local file for first 5MB at position: $position")
        } else {
            Timber.tag("ReelAdapter").d("Using remote URL at position: $position")
        }

        val dataSourceFactory = dataSourceFactories[position] ?: DataSource.Factory {
            if (localFile != null && localFile.exists()) {
                CacheDataSource(
                    mediaCache.obtainCache(),
                    DefaultHttpDataSource.Factory().createDataSource(),
                    FileDataSource.Factory().createDataSource(),
                    null,
                    CacheDataSource.FLAG_BLOCK_ON_CACHE,
                    null
                )
            } else {
                DefaultHttpDataSource.Factory().createDataSource()
            }
        }.also {
            dataSourceFactories[position] = it
        }

        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(MediaItem.fromUri(remoteUri))
    }

    private fun downloadVideoPartial(
        position: Int,
        onComplete: (File?) -> Unit = {}
    ) {
        if (position < 0 || position >= reels.size) {
            Timber.tag("ReelAdapter").w("Invalid position for download: $position")
            onComplete(null)
            return
        }

        val videoUrl = reels[position].videoUrl
        if (videoUrl.isEmpty()) {
            Timber.tag("ReelAdapter").w("Invalid video URL at position $position")
            onComplete(null)
            return
        }

        val videoFile = File(context.cacheDir, "video_${position}_partial.mp4")
        if (videoFile.exists()) {
            Timber.tag("ReelAdapter").d("First 5MB of video at position $position already downloaded")
            downloadedFiles[position] = videoFile
            if (!mediaSources.containsKey(position)) {
                mediaSources[position] = createMediaSource(position)
            }
            onComplete(videoFile)
            return
        }

        val latch = CountDownLatch(1)
        downloadLatches[position] = latch

        val downloadTask = Thread {
            val maxBytes = 5 * 1024 * 1024 // 5MB
            try {
                val url = URL(videoUrl)
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("Range", "bytes=0-${maxBytes}")
                connection.connect()
                if (connection.responseCode != HttpURLConnection.HTTP_PARTIAL && connection.responseCode != HttpURLConnection.HTTP_OK) {
                    throw Exception("HTTP error code: ${connection.responseCode}")
                }

                val input = connection.inputStream
                val output = FileOutputStream(videoFile)
                val buffer = ByteArray(1024)
                var bytesRead: Int
                var totalBytesRead = 0L
                while (input.read(buffer).also { bytesRead = it } != -1 && totalBytesRead < maxBytes) {
                    output.write(buffer, 0, bytesRead)
                    totalBytesRead += bytesRead
                }
                output.close()
                input.close()
                connection.disconnect()

                Timber.tag("ReelAdapter").d("Successfully downloaded first 5MB for position $position ($totalBytesRead bytes)")
                downloadedFiles[position] = videoFile
                mediaSources[position] = createMediaSource(position)

                cleanupOldFiles(position)
                onComplete(videoFile)
            } catch (e: Exception) {
                Timber.tag("ReelAdapter").e(e, "Error downloading first 5MB of video at position $position ${e.message}")
                videoFile.delete()
                onComplete(null)
            } finally {
                latch.countDown()
                downloadLatches.remove(position)
            }
        }
        downloadTask.start()
    }

    private fun setPlayVideoWhenSwitchedSource(
        player: ExoPlayer,
        playerView: PlayerView,
        position: Int
    ) {
        val localMediaSource = createMediaSource(position)
        val currentPositionMs = player.currentPosition
        player.setMediaSource(localMediaSource)
        player.playWhenReady = true
        player.seekTo(currentPositionMs)
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
        player.play()

        // Đảm bảo PlayerView hiển thị và surface được làm mới
        playerView.setVisibility(View.VISIBLE)

        Timber.tag("ReelAdapter").d("Switched to local file after retry at position: $position localFile: ${downloadedFiles[position]?.path}")
    }

    // khoanh vùng vị trí hợp lệ tải trước
    private fun positionsToKeep(currentPosition: Int): Set<Int> {
        val range = (currentPosition - 3..currentPosition + 3)
            .filter { it >= 0 && it < reels.size }
            .toSet()
        return range
    }

    private fun downloadNextVideoPartial(currentPosition: Int) {
        // Chỉ tải trước cho 2 position tiếp theo
        val positionsToPreload = listOf(currentPosition + 1, currentPosition + 2)
            .filter { it >= 0 && it < reels.size && !downloadedFiles.containsKey(it) }

        for (position in positionsToPreload) {
            downloadVideoPartial(position)
        }

        // Hủy tải các position không cần thiết
        val positionsToKeep = positionsToKeep(currentPosition)
        downloadLatches.keys.toList().forEach { pos ->
            if (pos !in positionsToKeep) {
                downloadLatches.remove(pos)?.countDown()
                Timber.tag("ReelAdapter").d("Canceled download for position: $pos")
            }
        }
    }

    private fun cleanupOldFiles(currentPosition: Int) {
        // Giữ lại file của position hiện tại và các position gần đó
        val positionsToKeep = positionsToKeep(currentPosition)

        val iterator = downloadedFiles.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val position = entry.key
            if (position !in positionsToKeep) {
                val file = entry.value
                file.delete()
                iterator.remove()
                mediaSources.remove(position)
                dataSourceFactories.remove(position)
                Timber.tag("ReelAdapter").d("Deleted old partial video file for position $position")
            }
        }
    }

    fun releaseAllPlayers() {
        playLock.lock()
        try {
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val holder = recyclerView.getChildViewHolder(child) as? ReelViewHolder
                holder?.releasePlayer()
            }
            downloadedFiles.values.forEach { it.delete() }
            downloadedFiles.clear()
            mediaSources.clear()
            downloadLatches.clear()
            dataSourceFactories.clear()
            activePlayers.clear()
            Timber.tag("ReelAdapter").d("Cleared all downloaded partial files and media sources")
        } finally {
            playLock.unlock()
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
        val playerView: PlayerView = itemView.findViewById(R.id.player_view)
        private val tvReelInfo: TextView = itemView.findViewById(R.id.tv_reel_info)
        var player: ExoPlayer? = null
        private var retryCount = 0
        private val maxRetries = 3

        init {
            player = ExoPlayer.Builder(context)
                .setLoadControl(
                    DefaultLoadControl.Builder()
                        .setBufferDurationsMs(3000, 30000, 3000, 3000)
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
                            Toast.makeText(context, "Error playing video at position $bindingAdapterPosition", Toast.LENGTH_SHORT).show()

                            if (retryCount < maxRetries && bindingAdapterPosition == currentPlayingPosition) {
                                retryCount++
                                Timber.tag("ReelAdapter").d("Retrying playback at position $bindingAdapterPosition, attempt $retryCount")
                                postDelayed({
                                    val remoteUri = Uri.parse(reels[bindingAdapterPosition].videoUrl)
                                    val mediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                                        .createMediaSource(MediaItem.fromUri(remoteUri))
                                    setMediaSource(mediaSource)
                                    prepare()
                                    play()
                                }, 500)
                            } else {
                                retryCount = 0
                                Timber.tag("ReelAdapter").w("Max retries reached at position $bindingAdapterPosition, skipping")
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

            // Đảm bảo player không phát ngầm khi bind
            player?.let {
                it.stop()
                it.clearMediaItems()
                it.repeatMode = Player.REPEAT_MODE_OFF
                Timber.tag("ReelAdapter").d("Cleared player at position $position during bind")
            }

            tvReelInfo.text = """
                ID: ${reel.id}
                Likes: ${reel.likeCount}
                Comments: ${reel.commentCount}
                Views: ${reel.viewCount}
                Tags: ${reel.tags?.joinToString() ?: "None"}
            """.trimIndent()
        }

        fun releasePlayer() {
            if (bindingAdapterPosition != RecyclerView.NO_POSITION) {
                activePlayers.remove(bindingAdapterPosition)
                Timber.tag("ReelAdapter").d("Removed player from activePlayers at position $bindingAdapterPosition, activePlayers size: ${activePlayers.size}")
            }
            player?.release()
            player = null
            playerView.player = null
            Timber.tag("ReelAdapter").d("Released player at position $bindingAdapterPosition")
        }

        private fun postDelayed(action: () -> Unit, delayMs: Long) {
            playerView.postDelayed(action, delayMs)
        }
    }
}