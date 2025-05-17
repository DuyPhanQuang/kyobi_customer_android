package com.kyobi.trend.performance_metrics

import androidx.annotation.OptIn
import androidx.media3.exoplayer.analytics.PlaybackStats
import androidx.media3.exoplayer.analytics.PlaybackStatsListener
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
class VideoPerformanceTracker(
    val videoPlayerPerformanceMetricsListener: VideoPlayerPerformanceMetricsListener = VideoPlayerPerformanceMetricsListener(),
    val playbackStatsListener: PlaybackStatsListener = PlaybackStatsListener(
        /* keepHistory= */ false,
        /* callback= */ null,
    ),
) {

    fun getResult(): VideoPerformanceTrackerResult {
        val stats = playbackStatsListener.playbackStats
        return VideoPerformanceTrackerResult(
            videoPerformanceData = videoPlayerPerformanceMetricsListener.videoPerformanceData,
            exoPlayerStats = stats,
        )
    }

    fun invalidateSession() {
        videoPlayerPerformanceMetricsListener.invalidate()
        // playback stats listener is invalidated automatically as it's tied to exo player session
    }
}

@OptIn(UnstableApi::class)
data class VideoPerformanceTrackerResult(
    val videoPerformanceData: VideoPerformanceData,
    val exoPlayerStats: PlaybackStats?
)