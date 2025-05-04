package com.kyobi.trend.ui

import android.content.Context
import androidx.annotation.OptIn
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.kyobi.trend.cache.MediaCache
import com.kyobi.trend.model.Reel
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import java.util.UUID
import java.util.concurrent.Executors
import javax.inject.Inject

@HiltViewModel
class ReelPlaybackViewModel @OptIn(UnstableApi::class)
@Inject constructor(
    @ApplicationContext private val context: Context,
    val mediaCache: MediaCache
) : ViewModel() {
    private val tag = "ReelPlaybackViewModel"
    private val _reels = mutableStateOf<List<Reel>>(emptyList())
    val reels: State<List<Reel>> = _reels
    val shortenMediaSources = mutableMapOf<String, MediaSource?>()
    val fullMediaSources = mutableMapOf<String, MediaSource?>()
    private val executorService = Executors.newFixedThreadPool(2)

    fun setReels(newReels: List<Reel>) {
        Timber.tag(tag).d("Setting reels, size: ${newReels.size}")
        _reels.value = newReels
    }

    fun fetchMoreReels() {
        // Mock fetch thêm
        val newReels = List(5) { index ->
            Reel(
                id = "reel_${UUID.randomUUID()}",
                videoUrl = "https://ilwzwgxiwwejqncfvysv.supabase.co/storage/v1/object/sign/reels/468b4d36-4d43-48f3-8f32-0d796e2b2ecd/video-1744549928872.mp4?token=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1cmwiOiJyZWVscy80NjhiNGQzNi00ZDQzLTQ4ZjMtOGYzMi0wZDc5NmUyYjJlY2QvdmlkZW8tMTc0NDU0OTkyODg3Mi5tcDQiLCJpYXQiOjE3NDYzNDA0MDcsImV4cCI6MTc0ODkzMjQwN30.uJl1iedItiAdWgaEpHrEK8hUDhWPq55Hf0YH3SIqfKM",
                shortenUrl = "",
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

    fun preloadShortenAndFullMediaSources(page: Int) {
        if (page >= reels.value.size) return
        val reel = reels.value[page]
        // Preload shortenUrl
        if (!shortenMediaSources.containsKey(reel.shortenUrl) && reel.shortenUrl.isNotEmpty()) {
            executorService.execute {
                try {
                    val mediaItem = MediaItem.fromUri(reel.shortenUrl).buildUpon()
                        .setMediaId(reel.shortenUrl).build()
                    val mediaSource = startCreateMediaSource(mediaItem)
                    shortenMediaSources[reel.shortenUrl] = mediaSource
                    Timber.tag(tag).d("Preloaded shortenUrl MediaSource for page $page")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to preload shortenUrl MediaSource for page $page")
                }
            }
        }
        // Preload videoUrl
        if (!fullMediaSources.containsKey(reel.videoUrl)) {
            executorService.execute {
                try {
                    val mediaItem = MediaItem.fromUri(reel.videoUrl).buildUpon()
                        .setMediaId(reel.videoUrl).build()
                    val mediaSource = startCreateMediaSource(mediaItem)
                    fullMediaSources[reel.videoUrl] = mediaSource
                    Timber.tag(tag).d("Preloaded videoUrl MediaSource for page $page")
                } catch (e: Exception) {
                    Timber.tag(tag).e(e, "Failed to preload videoUrl MediaSource for page $page")
                }
            }
        }
    }

    @OptIn(UnstableApi::class)
    fun startCreateMediaSource(mediaItem: MediaItem): MediaSource {
        try {
            val uri = mediaItem.localConfiguration?.uri.toString()
            Timber.tag(tag).d("Creating MediaSource for URI: $uri")
            val cacheDataSourceFactory = mediaCache.createSharedCacheDataSourceFactory(
                context, mediaCache.getCache())
            return if (uri.endsWith(".m3u8")) {
                HlsMediaSource.Factory(cacheDataSourceFactory)
                    .setAllowChunklessPreparation(true)
                    .createMediaSource(mediaItem)
            } else {
                ProgressiveMediaSource.Factory(cacheDataSourceFactory)
                    .createMediaSource(mediaItem)
            }
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Failed to create mediaSource")
            throw e
        }
    }

    override fun onCleared() {
        Timber.tag(tag).d("ViewModel cleared")
        super.onCleared()
    }
}