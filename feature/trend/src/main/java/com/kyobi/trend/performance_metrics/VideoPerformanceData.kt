package com.kyobi.trend.performance_metrics

data class VideoPerformanceData(
    val decoders: Decoders,
    val loadTime: LoadTime,
    val videoInfo: VideoInfo,
)