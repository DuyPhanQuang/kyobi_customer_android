package com.kyobi.trend.performance_metrics

import androidx.annotation.OptIn
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import timber.log.Timber

const val DEFAULT_VALUE = -1L
const val metricTag = "VideoPlayerPerformanceMetricsListener"

@OptIn(UnstableApi::class)
class VideoPlayerPerformanceMetricsListener : AnalyticsListener {
    private val loadTime = LoadTime()
    private val decoders = Decoders()
    private val videoInfo = VideoInfo()

    val videoPerformanceData: VideoPerformanceData
        get() = VideoPerformanceData(
            videoInfo = videoInfo,
            decoders = decoders,
            loadTime = loadTime,
        )

    override fun onAudioDecoderReleased(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
    ) {
        super.onAudioDecoderReleased(eventTime, decoderName)
        decoders.onAudioDecoderReleased(
            realtimeMs = eventTime,
            decoderName = decoderName,
        )
    }

    override fun onVideoDecoderReleased(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
    ) {
        super.onVideoDecoderReleased(eventTime, decoderName)
        decoders.onVideoDecoderReleased(
            realtimeMs = eventTime,
            decoderName = decoderName,
        )
    }

    @Deprecated("Deprecated in Java")
    override fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        Timber.tag(metricTag).d("onLoadStarted: dataType=${mediaLoadData.dataType}, uri=${loadEventInfo.dataSpec.uri}")
        loadTime.onLoadStarted(
            eventTime = eventTime,
            loadEventInfo = loadEventInfo,
            mediaLoadData = mediaLoadData,
        )
    }

    override fun onLoadCompleted(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
    ) {
        Timber.tag(metricTag).d("onLoadCompleted: dataType=${mediaLoadData.dataType}, uri=${loadEventInfo.dataSpec.uri}")
        loadTime.onLoadCompleted(
            loadTime = loadEventInfo,
            eventTime = eventTime,
            mediaLoadData = mediaLoadData,
        )
    }

    override fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        output: Any,
        renderTimeMs: Long,
    ) {
        super.onRenderedFirstFrame(eventTime, output, renderTimeMs)
        loadTime.onRenderedFirstFrame(
            eventTime = eventTime,
            output = output,
            renderTimeMs = renderTimeMs,
        )
        Timber.tag(metricTag).d("onRenderedFirstFrame: renderTimeMs=$renderTimeMs, timestamp=${System.currentTimeMillis()}")
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        super.onPlaybackStateChanged(eventTime, state)
        loadTime.onVideoStartedOrFullyReadyToBePlayed(state)
    }

    override fun onAudioDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long,
    ) {
        super.onAudioDecoderInitialized(
            eventTime,
            decoderName,
            initializedTimestampMs,
            initializationDurationMs,
        )
        decoders.onAudioDecoderInitialized(
            eventTime = eventTime,
            timestamp = initializedTimestampMs,
            initializationDurationMs = initializationDurationMs,
            decoderName = decoderName,
        )
    }

    override fun onVideoDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long,
    ) {
        decoders.onVideoDecoderInitialised(
            eventTime = eventTime,
            decoderName = decoderName,
            initializedTimestampMs = initializedTimestampMs,
            initializationDurationMs = initializationDurationMs,
        )
    }

    override fun onTracksChanged(eventTime: AnalyticsListener.EventTime, tracks: Tracks) {
        super.onTracksChanged(eventTime, tracks)
        videoInfo.onTracksChanged(
            eventTime = eventTime,
            tracks = tracks,
        )
    }

    fun invalidate() {
        loadTime.invalidate()
        decoders.invalidate()
        videoInfo.invalidate()
    }
}