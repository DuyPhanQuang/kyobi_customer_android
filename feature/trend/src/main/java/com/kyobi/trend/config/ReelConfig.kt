package com.kyobi.trend.config

data class ReelConfig(
    val bufferMinMs: Int = 2000, // Buffer tối thiểu của ExoPlayer
    val bufferMaxMs: Int = 6000, // Buffer tối đa của ExoPlayer
    val bufferPlaybackMs: Int = 2000, // Buffer cho playback
    val bufferRebufferMs: Int = 2000, // Buffer cho rebuffer
)