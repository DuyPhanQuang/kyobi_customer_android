package com.kyobi.trend.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
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
    private val mediaSources = mutableMapOf<Int, MediaSource>()
    private lateinit var pool: PlayerPool

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        reels.clear()
        reels.addAll(newReels)
    }

    /**
     * Khởi tạo PlayerPool sau khi MediaSource đã preload xong.
     */
    fun initPlayerPool(
        preloadedMediaItems: MutableMap<Int, MediaItem>,
        preloadedMediaSources: MutableMap<Int, MediaSource>
    ): PlayerPool? {
        try {
            for (i in reels.indices) {
                mediaItems[i] = preloadedMediaItems[i]!!
                mediaSources[i] = preloadedMediaSources[i]!!
            }
            // tạo pool
            pool = PlayerPool(context)
            Timber.tag(tag).d("PlayerPool initialized with ${reels.size} videos")

            return pool
        } catch (e: Exception) {
            Timber.tag(tag).d("Failed to initPlayerPool e: ${e.message}")
            return null
        }
    }

    fun getCurrentPlayingPosition(): Int = currentPlayingPosition

    fun getMediaSources(): MutableMap<Int, MediaSource> = mediaSources

    fun updateSurfaceReadyState(position: Int, isReady: Boolean) {
        surfaceReadyStates[position] = isReady
        Timber.tag(tag).d("Surface ready state updated for position $position: $isReady")
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
                    .setAllowChunklessPreparation(true) // Giảm tải cho HLS
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
        currentPlayingPosition = position
    }

    fun onPlayerReleased(position: Int) {
        surfaceReadyStates.remove(position)
        mediaItems.remove(position)
        mediaSources.remove(position)
    }

    override fun onCleared() {
        super.onCleared()
        surfaceReadyStates.clear()
        mediaItems.clear()
        mediaSources.clear()
        pool.releaseAll()
    }
}