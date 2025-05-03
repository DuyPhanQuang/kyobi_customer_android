package com.kyobi.trend.ui

import android.content.Context
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@UnstableApi
@HiltViewModel
class ReelPlaybackViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: MediaCache
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    val reels = mutableStateOf<List<Reel>>(emptyList())
    val playerPool = mutableStateOf<PlayerPool?>(null)
    val currentPosition = mutableIntStateOf(0)

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        reels.value = newReels
        viewModelScope.launch(Dispatchers.IO) {
            newReels.forEachIndexed { index, reel ->
                Timber.tag(tag).d("Reel[$index]: videoUrl=${reel.videoUrl}, thumbnailUrl=${reel.thumbnailUrl}")
            }
        }
    }

    fun initPlayerPool() {
        if (playerPool.value == null) {
            playerPool.value = PlayerPool(context)
            Timber.tag(tag).d("PlayerPool initialized")
        }
    }

    fun onPageSelected(position: Int) {
        currentPosition.intValue = position
        Timber.tag(tag).d("Page selected: $position")
    }

    fun getMediaSource(position: Int): MediaSource? {
        if (position < 0 || position >= reels.value.size) {
            Timber.tag(tag).w("Invalid position: $position")
            return null
        }
        val reel = reels.value[position]
        val mediaItem = MediaItem.fromUri(reel.videoUrl).buildUpon()
            .setMediaId(reel.videoUrl).build()
        return startCreateMediaSource(mediaItem).also {
            Timber.tag(tag).d("getMediaSource for position=$position, mediaId=${it?.mediaItem?.mediaId}")
        }
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
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(mediaItem)
            } else {
                Timber.tag(tag).d("Creating ProgressiveMediaSource for URI: $uri")
                ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(mediaItem)
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to create MediaSource for $mediaItem")
            return null
        }
    }

    override fun onCleared() {
        playerPool.value?.releaseAll()
        Timber.tag(tag).d("ViewModel cleared")
    }
}