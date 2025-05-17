package com.kyobi.trend.performance_metrics

import androidx.annotation.OptIn
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
open class Decoders {
    open val videoDecoderInitialisationTimestamp: Long
        get() = _videoDecoderInitialisationTimestamp

    open val videoDecoderInitialisationDurationMs: Float
        get() = _videoDecoderInitialisationDurationMs

    open val videoDecoderName: String
        get() = _videoDecoderName

    open val audioDecoderInitialisationDurationMs: Float
        get() = _audioDecoderInitialisationDurationMs

    open val audioDecoderInitialisationTimestamp: Long
        get() = _audioDecoderInitialisationTimestamp

    open val audioDecoderName: String
        get() = _audioDecoderName

    private var _videoDecoderInitialisationTimestamp: Long = DEFAULT_VALUE
    private var _videoDecoderInitialisationDurationMs: Float = 0f
    private var _videoDecoderName: String = ""

    private var _audioDecoderInitialisationDurationMs: Float = 0f
    private var _audioDecoderName: String = ""
    private var _audioDecoderInitialisationTimestamp: Long = DEFAULT_VALUE

    fun onAudioDecoderReleased(realtimeMs: AnalyticsListener.EventTime, decoderName: String) {
        _audioDecoderName = ""
    }

    fun onVideoDecoderReleased(realtimeMs: AnalyticsListener.EventTime, decoderName: String) {
        _videoDecoderName = ""
    }

    fun onAudioDecoderInitialized(
        timestamp: Long,
        initializationDurationMs: Long,
        decoderName: String,
        eventTime: AnalyticsListener.EventTime,
    ) {
        _audioDecoderInitialisationTimestamp = System.currentTimeMillis()
        _audioDecoderInitialisationDurationMs = initializationDurationMs.toFloat()
        _audioDecoderName = decoderName
    }

    fun onVideoDecoderInitialised(
        initializedTimestampMs: Long,
        initializationDurationMs: Long,
        decoderName: String,
        eventTime: AnalyticsListener.EventTime,
    ) {
        _videoDecoderInitialisationTimestamp = System.currentTimeMillis()
        _videoDecoderInitialisationDurationMs = initializationDurationMs.toFloat()
        _videoDecoderName = decoderName
    }

    fun invalidate() {
        _videoDecoderInitialisationTimestamp = DEFAULT_VALUE
        _audioDecoderInitialisationTimestamp = DEFAULT_VALUE
        _videoDecoderInitialisationDurationMs = 0f
        _audioDecoderInitialisationDurationMs = 0f
        _videoDecoderName = ""
        _audioDecoderName = ""
    }
}