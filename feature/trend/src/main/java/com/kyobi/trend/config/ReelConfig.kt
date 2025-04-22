package com.kyobi.trend.config

data class ReelConfig(
    val downloadSizeMb: Int = 5, // Số MB tải trước cho mỗi video
    val positionsToKeepRange: Int = 3, // Range của positionsToKeep (±3)
    val bufferMinMs: Int = 5000, // Buffer tối thiểu của ExoPlayer
    val bufferMaxMs: Int = 50000, // Buffer tối đa của ExoPlayer
    val bufferPlaybackMs: Int = 5000, // Buffer cho playback
    val bufferRebufferMs: Int = 5000, // Buffer cho rebuffer
    val cacheSizeMb: Int = 50 // Kích thước tối đa của MediaCache
)