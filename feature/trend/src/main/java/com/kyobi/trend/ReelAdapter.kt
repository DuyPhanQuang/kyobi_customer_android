package com.kyobi.trend

import android.content.Context
import android.graphics.Color
import android.net.Uri
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
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

@UnstableApi
class ReelAdapter(
    private val reels: List<Reel>,
    private val context: Context,
    private val mediaCache: MediaCache,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<ReelAdapter.ReelViewHolder>() {

    private var currentPlayer: ExoPlayer? = null
    private var currentPlayingPosition: Int = -1
    private val downloadedFiles = mutableMapOf<Int, File>() // Lưu file đã tải (5MB đầu tiên)
    private val mediaSources = mutableMapOf<Int, MediaSource>() // Lưu MediaSource đã preload
    private val downloadLatches = mutableMapOf<Int, CountDownLatch>() // Lưu trạng thái tải file
    private val playLock = ReentrantLock() // Lock để đồng bộ playVideoAtPosition
    private var lastPlayRequestTime = 0L // Thời gian gọi playVideoAtPosition cuối cùng
    private val debounceInterval = 500L // Khoảng thời gian tối thiểu giữa các lần gọi (500ms)
    private val dataSourceFactories = mutableMapOf<Int, DataSource.Factory>()

    init {
        currentPlayer = ExoPlayer.Builder(context)
            .setLoadControl(
                DefaultLoadControl.Builder()
                    .setBufferDurationsMs(10000, 30000, 5000, 5000)
                    .build()
            )
            .build()
            .apply {
                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(state: Int) {
                        Timber.tag("ReelAdapter").d("Player state at position $currentPlayingPosition: $state")
                    }
                    override fun onPlayerError(error: PlaybackException) {
                        Timber.tag("ReelAdapter").e("Error playing video at position $currentPlayingPosition: ${error.message}")
                        Toast.makeText(context, "Error playing video at position $currentPlayingPosition", Toast.LENGTH_SHORT).show()
                    }
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        Timber.tag("ReelAdapter").d("Is playing at position $currentPlayingPosition: $isPlaying")
                    }
                })
            }
    }

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
        // Debounce: Bỏ qua nếu thời gian giữa các lần gọi quá ngắn
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastPlayRequestTime < debounceInterval) {
            Timber.tag("ReelAdapter").d("Debouncing playVideoAtPosition for position: $position, currentPlayingPosition: $currentPlayingPosition")
            return
        }
        lastPlayRequestTime = currentTime

        // Kiểm tra điều kiện cơ bản
        if (position < 0 || position >= reels.size) {
            Timber.tag("ReelAdapter").d("Invalid position: $position, skipping playVideoAtPosition")
            return
        }

        // Bỏ qua nếu ExoPlayer đang phát hoặc chuẩn bị cho position này
        currentPlayer?.let { player ->
            if (position == currentPlayingPosition &&
                (player.playbackState == Player.STATE_BUFFERING || player.playbackState == Player.STATE_READY) &&
                player.playWhenReady
            ) {
                Timber.tag("ReelAdapter").d("Player already playing or buffering at position: $position, skipping")
                return
            }
        }

        // Đồng bộ để tránh race condition khi scroll nhanh
        if (playLock.tryLock()) {
            try {
                Timber.tag("ReelAdapter")
                    .d("Playing video at position: $position, currentPlayingPosition: $currentPlayingPosition")

                // Không cần xóa player của tất cả PlayerView hiện tại
                // Chỉ kiểm tra và log
                for (i in 0 until recyclerView.childCount) {
                    val child = recyclerView.getChildAt(i)
                    val holder = recyclerView.getChildViewHolder(child) as? ReelViewHolder
                    val childPosition = recyclerView.getChildAdapterPosition(child)
                    if (holder != null && childPosition != position) {
                        Timber.tag("ReelAdapter").d("Clearing PlayerView at position: $childPosition")
                        holder.playerView.setVisibility(View.INVISIBLE) // Ẩn PlayerView để giữ frame cuối
                    }
                }

                // Reset trạng thái ExoPlayer an toàn
                currentPlayer?.let { player ->
                    if (player.playbackState != Player.STATE_IDLE) {
                        // Tạm dừng player để các PlayerView cũ không phát tiếp
                        player.pause()
                        player.stop()
                    }

                    player.clearMediaItems()
                    player.repeatMode = Player.REPEAT_MODE_OFF
                }

                currentPlayingPosition = position

                // Chờ file local tải xong (nếu đang tải)
                val latch = downloadLatches[position]
                if (latch != null) {
                    Timber.tag("ReelAdapter").d("Waiting for download to complete at position: $position")
                    latch.await(2, TimeUnit.SECONDS) // Chờ tối đa 2 giây
                }

                // Lấy hoặc tạo MediaSource
                val mediaSource = mediaSources[position] ?: createMediaSource(position).also {
                    mediaSources[position] = it
                }

                // Cập nhật MediaSource cho ExoPlayer
                // Đảm bảo phát lại từ đầu
                // Luôn gọi play() để chắc chắn video được phát
                currentPlayer?.apply {
                    setMediaSource(mediaSource)
                    playWhenReady = true
                    volume = 1f
                    repeatMode = Player.REPEAT_MODE_ONE
                    prepare()
                    Timber.tag("ReelAdapter").d("Prepared player at position: $position")
                    seekTo(0)
                    play()
                    Timber.tag("ReelAdapter").d("Played player at position: $position")
                }

                // Cập nhật PlayerView
                val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelViewHolder
                if (holder != null) {
                    Timber.tag("ReelAdapter").d("Holder found at position: $position, updating PlayerView")
                    // đảm bảo surface của PlayerView được làm mới bang cách
                    // Làm mới surface của PlayerView tại position mới
                    holder.playerView.setVisibility(View.INVISIBLE)
                    // Tạm thời set player = null để release surface cũ
                    holder.playerView.player = null
                    holder.player = null
                    // Gán lại currentPlayer để làm mới surface
                    holder.playerView.player = currentPlayer
                    holder.player = currentPlayer
                    holder.playerView.setVisibility(View.VISIBLE)
                    holder.playerView.requestLayout() // Làm mới surface của PlayerView
                    holder.playerView.invalidate() // Đảm bảo surface được vẽ lại

                    // cấu hình lại PlayerView
                    configPlayerView(holder.playerView)
                } else {
                    Timber.tag("ReelAdapter").w("Holder not found at position: $position")
                }

                // Tải 5MB đầu tiên của video tiếp theo và preload MediaSource
                downloadNextVideoPartial(position)
            } catch (e: Exception) {
                Timber.tag("ReelAdapter").e("Error in playVideoAtPosition for position $position: ${e.message}", e)
            } finally {
                playLock.unlock()
            }
        } else {
            Timber.tag("ReelAdapter").w("Play lock not acquired, skipping playVideoAtPosition for position: $position")
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

        // Tạo DataSource cho file local (nếu có) và remote
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

    private fun downloadNextVideoPartial(currentPosition: Int) {
        val nextPosition = currentPosition + 1
        if (nextPosition < reels.size) {
            Timber.tag("ReelAdapter").d("Downloading first 5MB of video at position: $nextPosition")
            val nextVideoUrl = reels[nextPosition].videoUrl

            // Kiểm tra URL hợp lệ
            if (nextVideoUrl.isEmpty()) {
                Timber.tag("ReelAdapter").w("Invalid video URL at position $nextPosition")
                return
            }

            // Tạo file trong thư mục cache
            val videoFile = File(context.cacheDir, "video_${nextPosition}_partial.mp4")
            if (videoFile.exists()) {
                Timber.tag("ReelAdapter").d("First 5MB of video at position $nextPosition already downloaded")
                downloadedFiles[nextPosition] = videoFile
                // Preload MediaSource nếu chưa có
                if (!mediaSources.containsKey(nextPosition)) {
                    mediaSources[nextPosition] = createMediaSource(nextPosition)
                }
                return
            }

            // Tạo CountDownLatch để đồng bộ
            val latch = CountDownLatch(1)
            downloadLatches[nextPosition] = latch

            // Tải 5MB đầu tiên của video về local
            val downloadTask = Thread {
                val maxBytes = 5 * 1024 * 1024 // 5MB
                try {
                    val url = URL(nextVideoUrl)
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

                    Timber.tag("ReelAdapter").d("Successfully downloaded first 5MB for position $nextPosition ($totalBytesRead bytes)")
                    downloadedFiles[nextPosition] = videoFile

                    // Preload MediaSource sau khi tải xong
                    mediaSources[nextPosition] = createMediaSource(nextPosition)

                    // Xóa các file không cần thiết
                    cleanupOldFiles(currentPosition)
                } catch (e: Exception) {
                    Timber.tag("ReelAdapter").e("Error downloading first 5MB of video at position $nextPosition: ${e.message}", e)
                    videoFile.delete() // Xóa file nếu tải lỗi
                } finally {
                    latch.countDown() // Đánh dấu tải xong
                    downloadLatches.remove(nextPosition)
                }
            }
            downloadTask.start()
        }
    }

    private fun cleanupOldFiles(currentPosition: Int) {
        // Giữ file của currentPosition - 2, currentPosition - 1, currentPosition, currentPosition + 1, currentPosition + 2, currentPosition + 3
        val positionsToKeep = setOf(
            currentPosition - 2,
            currentPosition - 1,
            currentPosition,
            currentPosition + 1,
            currentPosition + 2,
            currentPosition + 3
        ).filter { it >= 0 && it < reels.size }.toSet()

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
            currentPlayer?.stop()
            currentPlayer?.release()
            currentPlayer = null
            currentPlayingPosition = -1

            // Xóa tất cả file và MediaSource
            downloadedFiles.values.forEach { it.delete() }
            downloadedFiles.clear()
            mediaSources.clear()
            downloadLatches.clear()
            dataSourceFactories.clear()
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

        init {
            configPlayerView(playerView)
        }

        fun bind(reel: Reel, position: Int) {
            Timber.tag("ReelAdapter").d("Binding position: $position, player exists: ${player != null}")

            // Reset PlayerView khi tái sử dụng ViewHolder
            playerView.setVisibility(View.INVISIBLE)
            playerView.player = null
            player = null

            tvReelInfo.text = """
                ID: ${reel.id}
                Likes: ${reel.likeCount}
                Comments: ${reel.commentCount}
                Views: ${reel.viewCount}
                Tags: ${reel.tags?.joinToString() ?: "None"}
            """.trimIndent()
        }

        fun releasePlayer() {
            if (player != null && player != currentPlayer) {
                Timber.tag("ReelAdapter").d("Releasing Player at position $bindingAdapterPosition")
                playerView.setVisibility(View.INVISIBLE)
                playerView.player = null
                player = null
            }
        }
    }
}