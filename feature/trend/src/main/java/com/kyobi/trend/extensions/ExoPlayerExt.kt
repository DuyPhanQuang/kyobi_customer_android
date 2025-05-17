package com.kyobi.trend.extensions

import androidx.annotation.OptIn
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.util.UnstableApi
import com.kyobi.trend.performance_metrics.VideoPerformanceTracker

@OptIn(UnstableApi::class)
fun ExoPlayer.addPerformanceTracker(tracker: VideoPerformanceTracker) {
    val performanceListener = tracker.videoPlayerPerformanceMetricsListener
    addAnalyticsListener(performanceListener)

    val playbackStatsListener = tracker.playbackStatsListener
    addAnalyticsListener(playbackStatsListener)
}