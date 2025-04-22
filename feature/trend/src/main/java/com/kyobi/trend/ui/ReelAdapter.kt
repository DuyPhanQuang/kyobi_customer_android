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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.feature.trend.R
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.buffer
import okio.sink
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.abs

@UnstableApi
class ReelAdapter(
    val reels: List<Reel>,
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
    private val preloadMainHandler = Handler(Looper.getMainLooper()) // Thread để preload
    private val activePlayers = mutableMapOf<Int, ExoPlayer>() // Lưu các player đang hoạt động

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
                player.playWhenReady = false
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
        // Kiểm tra điều kiện cơ bản
        if (position < 0 || position >= reels.size) {
            Timber.tag("ReelAdapter").d("Invalid position: $position, skipping playVideoAtPosition")
            return
        }
        // Kiểm tra nếu position hiện tại đang phát và không cần reset
        if (position == currentPlayingPosition) {
            val currentPlayer = activePlayers[position]
            if (currentPlayer != null &&
                currentPlayer.isPlaying) {
                Timber.tag("ReelAdapter").d("Player at position $position is already playing, skipping")
                return
            }
        }
        // đảm bảo đồng bộ để tránh race condition khi scroll nhanh
        playLock.lock()
        try {
            Timber.tag("ReelAdapter").d("Before pausing other players, activePlayers: ${activePlayers.keys}")
            val positionsToKeep = positionsToKeep(position)
            // Dừng tất cả player trong activePlayers
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
            // Dừng tất cả player hiện tại trong RecyclerView
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val holder = recyclerView.getChildViewHolder(child) as? ReelViewHolder
                holder?.player?.let { player ->
                    val pos = holder.bindingAdapterPosition
                    if (pos != position &&
                        pos != RecyclerView.NO_POSITION) {
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
            // Xóa các player ngoài khoảng cách ±10 so với vị trí hiện tại
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
            // Phát ExoPlayer tại position mới
            val holder = recyclerView.findViewHolderForAdapterPosition(position) as? ReelViewHolder
            if (holder != null) {
                holder.player?.let { player ->
                    // Thêm player vào activePlayers khi phát
                    activePlayers[position] = player
                    Timber.tag("ReelAdapter").d("Added player to activePlayers at position $position, activePlayers size: ${activePlayers.size}")

                    // Nếu player đã có trạng thái (đã phát trước đó), không reset mà tiếp tục phát
                    if (player.playbackState == Player.STATE_ENDED || player.playbackState == Player.STATE_IDLE) {
                        player.repeatMode = Player.REPEAT_MODE_OFF
                        player.stop()
                        player.clearMediaItems()
                        // Ban đầu luôn phát từ remote URL
                        val remoteMediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                            .createMediaSource(MediaItem.fromUri(reels[position].videoUrl))
                        player.setMediaSource(remoteMediaSource)
                    }

                    player.playWhenReady = true
                    player.volume = 1f
                    player.repeatMode = Player.REPEAT_MODE_ONE
                    player.prepare()
                    Timber.tag("ReelAdapter").d("Played player at position: $position")

                    // Kiểm tra và chuyển nguồn nếu file local đã tồn tại hoặc sau khi tải xong
                    val switchToLocalIfAvailable = {
                        Timber.tag("ReelAdapter").d("Checking downloadedFiles for position $position: containsKey=${downloadedFiles.containsKey(position)}, filePath=${downloadedFiles[position]?.path}")
                        // Chuyển nguồn nếu player đang ở trạng thái BUFFERING hoặc READY
                        val playbackState = player.playbackState

                        if (downloadedFiles.containsKey(position) && (playbackState == Player.STATE_BUFFERING || playbackState == Player.STATE_READY)) {
                            setPlayVideoWhenSwitchedSource(player, position)
                        } else {
                            Timber.tag("ReelAdapter").d("Did not switch to local file at position $position: containsKey=${downloadedFiles.containsKey(position)}, isPlaying=${player.isPlaying}")
                            // Kiểm tra lại sau x ms nếu chưa chuyển được
                            if (downloadedFiles.containsKey(position)) {
                                mainHandler.postDelayed({
                                    if (playbackState != Player.STATE_ENDED &&
                                        position == currentPlayingPosition) {
                                        Timber.tag("ReelAdapter").d("Retrying switch to local file at position: $position")
                                        val retryPlaybackState = player.playbackState
                                        if (downloadedFiles.containsKey(position) &&
                                            (retryPlaybackState == Player.STATE_BUFFERING || retryPlaybackState == Player.STATE_READY)) {
                                            setPlayVideoWhenSwitchedSource(player, position)
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
            // Dọn dẹp các file cũ không còn cần thiết trong IO thread
            // Mỗi khi người dùng chuyển sang position mới, các file cũ không còn nằm trong positionsToKeep (position ±3) cần được xóa để tiết kiệm bộ nhớ.
            // Gọi trước downloadNextVideoPartial đảm bảo rằng bộ nhớ được dọn dẹp trước khi tải file mới, tránh tình trạng bộ nhớ tăng không kiểm soát.
            CoroutineScope(Dispatchers.IO).launch {
                cleanupOldFiles(currentPlayingPosition)
            }
            // Preload videos
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
        // Dựa trên currentPlayingPosition để xác định các vị trí cần preload
        val preloadCount = 2
        val preloadStart = (currentPlayingPosition - preloadCount).coerceAtLeast(0)
        val preloadEnd = (currentPlayingPosition + preloadCount).coerceAtMost(reels.size - 1)
        // Preload MediaSource cho tất cả vị trí trong khoảng
        for (pos in preloadStart..preloadEnd) {
            if (pos !in firstVisiblePosition..lastVisiblePosition) {
                if (!mediaSources.containsKey(pos)) {
                    val mediaSource = createMediaSource(pos)
                    mediaSources[pos] = mediaSource
                    Timber.tag("ReelAdapter").d("Preloaded MediaSource for position $pos")
                }
            }
        }
        // Ưu tiên các vị trí gần currentPlayingPosition nhất, nhưng chưa được tải
        val positionsToPreload = (preloadStart..preloadEnd)
            .filter { pos ->
                pos !in firstVisiblePosition..lastVisiblePosition &&
                        !downloadedFiles.containsKey(pos) &&
                        !downloadLatches.containsKey(pos)
            }
            .sortedBy { abs(it - currentPlayingPosition) } // Sắp xếp theo khoảng cách đến currentPlayingPosition
            .take(2) // Giới hạn tối đa 2 vị trí preload đồng thời
        // Nếu không đủ 2 vị trí, mở rộng phạm vi tìm kiếm
        val additionalPositions = if (positionsToPreload.size < 2) {
            val extendedStart = (currentPlayingPosition - preloadCount - 1).coerceAtLeast(0)
            val extendedEnd = (currentPlayingPosition + preloadCount + 1).coerceAtMost(reels.size - 1)
            (extendedStart until preloadStart).union(preloadEnd + 1..extendedEnd)
                .filter { pos ->
                    !downloadedFiles.containsKey(pos) &&
                            !downloadLatches.containsKey(pos)
                }
                .sortedBy { abs(it - currentPlayingPosition) }
                .take(2 - positionsToPreload.size)
        } else {
            emptyList()
        }
        // Gộp danh sách các vị trí cần preload
        val finalPositionsToPreload = positionsToPreload + additionalPositions
        // Trì hoãn preload 5MB để tránh làm nặng main thread
        preloadMainHandler.removeCallbacksAndMessages(null)
        preloadMainHandler.postDelayed({
            finalPositionsToPreload.forEach { pos ->
                Timber.tag("ReelAdapter").d("Starting preload for position $pos in onScrolled")
                downloadVideoPartial(pos)
            }
        }, 200) // Delay 200ms để tránh trùng lặp
        // Hủy tải các vị trí không cần thiết
        val positionsToKeep = positionsToKeep(currentPlayingPosition)
        downloadLatches.keys.toList().forEach { pos ->
            if (pos !in positionsToKeep) {
                downloadLatches.remove(pos)?.countDown()
                Timber.tag("ReelAdapter").d("Canceled preload for position: $pos in onScrolled")
            } else {
                Timber.tag("ReelAdapter").d("Kept download latch for position: $pos in onScrolled")
            }
        }
        // Dọn dẹp MediaSource cũ
        mediaSources.keys.toList().forEach { pos ->
            if (pos !in positionsToKeep) {
                mediaSources.remove(pos)
                Timber.tag("ReelAdapter").d("Cleared MediaSource for position $pos in onScrolled")
            } else {
                Timber.tag("ReelAdapter").d("Kept MediaSource for position $pos in onScrolled")
            }
        }
        // Dọn dẹp file cũ
        CoroutineScope(Dispatchers.IO).launch {
            cleanupOldFiles(currentPlayingPosition)
        }
    }

    private fun createMediaSource(position: Int): MediaSource {
        val localFile = downloadedFiles[position]
        val remoteUri = Uri.parse(reels[position].videoUrl)
        // ko dùng localFile.exists() để check.
        // dựa vào downloadedFiles để quyết định sử dụng file local mà không cần gọi exists() (vì nếu file có trong downloadedFiles, khả năng cao nó đã được tải thành công).
        // bỏ localFile.exists() vì downloadedFiles chỉ chứa các file đã tải thành công (được thêm vào trong downloadVideoPartial).
        // điều này tránh thao tác I/O không cần thiết trên thread chính, giảm nguy cơ giật lag.
        if (localFile != null) {
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

    private fun downloadVideoPartial(position: Int, onComplete: () -> Unit = {}) {
        if (position < 0 || position >= reels.size) return
        // Kiểm tra xem file đã được tải chưa
        if (downloadedFiles.containsKey(position)) {
            Timber.tag("ReelAdapter").d("First 5MB of video at position $position already downloaded")
            onComplete()
            return
        }
        // Kiểm tra xem position đã bắt đầu tải hay chưa
        if (downloadLatches.containsKey(position)) {
            Timber.tag("ReelAdapter").d("Download already in progress for position $position, skipping")
            return
        }
        val latch = CountDownLatch(1)
        downloadLatches[position] = latch
        val url = reels[position].videoUrl
        val file = File(recyclerView.context.cacheDir, "video_${position}_partial.mp4")
        // Tạo OkHttpClient với timeout
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
        // Tạo request với Range header để tải xMB đầu tiên
        val maxBytes = 5 * 1024 * 1024 // 5MB
        val request = Request.Builder()
            .url(url)
            .header("Range", "bytes=0-${maxBytes - 1}")
            .build()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    response.body?.let { body ->
                        val sink = file.sink().buffer()
                        sink.writeAll(body.source())
                        sink.close()

                        val bytesDownloaded = file.length()
                        downloadedFiles[position] = file
                        Timber.tag("ReelAdapter").d("Successfully downloaded first 5MB for position $position ($bytesDownloaded bytes)")
                        onComplete()
                    } ?: run {
                        Timber.tag("ReelAdapter").w("Failed to download first 5MB for position $position: Empty response body")
                    }
                } else {
                    Timber.tag("ReelAdapter").w("Failed to download first 5MB for position $position: HTTP ${response.code}")
                }
            } catch (e: Exception) {
                Timber.tag("ReelAdapter").e(e, "Error downloading first 5MB for position $position")
            } finally {
                latch.countDown()
                downloadLatches.remove(position)
            }
        }
    }

    // lưu ý: giật lag khi chuyển nguồn
    // Khi chuyển nguồn (setPlayVideoWhenSwitchedSource), player được dừng tạm thời để thay đổi nguồn,
    // dẫn đến isPlaying=false trong thời gian ngắn. Sau đó, player tiếp tục phát (isPlaying=true).
    // Tác động: Không gây lỗi nghiêm trọng, nhưng có thể ảnh hưởng đến UX nếu có logic phụ thuộc vào isPlaying
    // (ví dụ, hiển thị UI "đang phát"). Usẻ có thể thấy video giật nhẹ trong khoảng thời gian chuyển nguồn.
    private fun setPlayVideoWhenSwitchedSource(player: ExoPlayer, position: Int) {
        val localMediaSource = createMediaSource(position)
        val currentPositionMs = player.currentPosition
        player.setMediaSource(localMediaSource)
        player.playWhenReady = true
        player.seekTo(currentPositionMs)
        player.repeatMode = Player.REPEAT_MODE_ONE
        player.prepare()
        Timber.tag("ReelAdapter").d("Switched to local file after retry at position: $position localFile: ${downloadedFiles[position]?.path}")
    }

    // khoanh vùng vị trí hợp lệ tải trước
    private fun positionsToKeep(currentPosition: Int): Set<Int> {
        val range = (currentPosition - 3..currentPosition + 3)
            .filter { it >= 0 && it < reels.size }
            .toSet()
        return range
    }

    private fun cleanupOldFiles(currentPosition: Int) {
        val positionsToKeep = positionsToKeep(currentPosition)
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

    fun releaseAllPlayers() {
        playLock.lock()
        try {
            for (i in 0 until recyclerView.childCount) {
                val child = recyclerView.getChildAt(i)
                val holder = recyclerView.getChildViewHolder(child) as? ReelViewHolder
                holder?.releasePlayer(true)
            }
            CoroutineScope(Dispatchers.IO).launch {
                cleanupOldFiles(currentPlayingPosition)
            }
            CoroutineScope(Dispatchers.IO).launch {
                downloadedFiles.values.forEach { it.delete() }
            }
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
        private val playerView: PlayerView = itemView.findViewById(R.id.player_view)
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
                                    CoroutineScope(Dispatchers.Default).launch {
                                        val remoteUri = Uri.parse(reels[bindingAdapterPosition].videoUrl)
                                        val mediaSource = ProgressiveMediaSource.Factory(DefaultHttpDataSource.Factory())
                                            .createMediaSource(MediaItem.fromUri(remoteUri))
                                        // Chuyển về thread chính để cập nhật player
                                        // (setMediaSource, prepare, play) phải chạy trên main thread vì ExoPlayer không thread-safe
                                        withContext(Dispatchers.Main) {
                                            Timber.tag("ReelAdapter").w("Started switching to main thread")
                                            setMediaSource(mediaSource)
                                            playWhenReady = true
                                            prepare()
                                        }
                                    }
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