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
import java.util.UUID
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

    fun fetchMoreReels() {
        // Mock fetch thêm 5 Reel mới
        val newReels = List(5) { index ->
            Reel(
                id = "reel_${UUID.randomUUID()}",
                videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1744549928872.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NDU0OTkyODg3Mi5tcDQiLCJpYXQiOjE3NDYzNDA0MDcsImV4cCI6MTc0ODkzMjQwN30.uJl1iedItiAdWgaEpHrEK8hUDhWPq55Hf0YH3SIqfKM",
                status = "PUBLISHED",
                likeCount = 800,
                commentCount = 90,
                shareCount = 30,
                viewCount = 3200,
                createdAt = "2025-04-19T15:30:00Z",
                thumbnailUrl = "https://media1.tenor.com/m/Gi3YSm0uDK0AAAAd/random-lol.gif",
                tags = listOf("style", "kyobi")
            )
        }
        _reels.value += newReels
        Timber.tag(tag).d("Fetched more reels, new size: ${_reels.value.size}")
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
        if (uri.endsWith(".mp4")) {
            cacheDataSourceFactory.setFlags(CacheDataSource.FLAG_BLOCK_ON_CACHE)
        } else {
            cacheDataSourceFactory.setFlags(CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR)
        }
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