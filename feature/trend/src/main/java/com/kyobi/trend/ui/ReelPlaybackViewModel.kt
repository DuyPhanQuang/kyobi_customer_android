package com.kyobi.trend.ui

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.CacheDataSink
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ReelPlaybackViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context,
    private val mediaCache: MediaCache
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private val _reels = mutableStateOf<List<Reel>>(emptyList())
    val reels: State<List<Reel>> = _reels

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        _reels.value = newReels
    }

    @OptIn(UnstableApi::class)
    fun startCreateMediaSource(mediaItem: MediaItem): MediaSource {
        val uri = mediaItem.localConfiguration?.uri.toString()
        Timber.tag(tag).d("Creating MediaSource for URI: $uri")
        val dataSourceFactory = DefaultDataSource.Factory(context)
        val cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(mediaCache.getCache())
            .setUpstreamDataSourceFactory(dataSourceFactory)
            .setCacheReadDataSourceFactory(dataSourceFactory)
            .setCacheWriteDataSinkFactory(CacheDataSink.Factory().setCache(mediaCache.getCache()))
        return if (uri.endsWith(".m3u8")) {
            HlsMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(mediaItem)
        } else {
            ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                .createMediaSource(mediaItem)
        }
    }

    override fun onCleared() {
        Timber.tag(tag).d("ViewModel cleared")
        super.onCleared()
    }
}