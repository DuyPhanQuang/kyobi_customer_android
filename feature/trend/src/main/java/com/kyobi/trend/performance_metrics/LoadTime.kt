package com.kyobi.trend.performance_metrics

import androidx.annotation.OptIn
import androidx.media3.common.C.DATA_TYPE_MANIFEST
import androidx.media3.common.C.DATA_TYPE_MEDIA
import androidx.media3.common.Player
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import androidx.media3.common.util.UnstableApi
import timber.log.Timber

@OptIn(UnstableApi::class)
open class LoadTime {
    open val networkOrCacheVideoLoadingStartedTimestamp: Long
        get() = _networkOrCacheVideoLoadingStartedTimestamp

    open val networkOrCacheVideoLoadingCompletedTimestamp: Long
        get() = _networkOrCacheVideoLoadingCompletedTimestamp

    open val networkOrCacheVideoLoadingDurationMs: Long
        get() = _networkOrCacheVideoLoadingDurationMs

    open val videoStartedOrFullyReadyToBePlayedTimestamp: Long
        get() = _videoStartedOrFullyReadyToBePlayedTimestamp

    open val videoFirstFrameRenderedTimestamp: Long
        get() = _videoFirstFrameRenderedTimestamp

    private var _networkOrCacheVideoLoadingStartedTimestamp: Long = DEFAULT_VALUE
    private var _networkOrCacheVideoLoadingCompletedTimestamp: Long = DEFAULT_VALUE
    private var _networkOrCacheVideoLoadingDurationMs: Long = 0L
    private var _videoStartedOrFullyReadyToBePlayedTimestamp: Long = DEFAULT_VALUE
    private var _videoFirstFrameRenderedTimestamp: Long = DEFAULT_VALUE

    internal fun onLoadStarted(
        eventTime: AnalyticsListener.EventTime,
        mediaLoadData: MediaLoadData,
        loadEventInfo: LoadEventInfo,
    ) {
        when (mediaLoadData.dataType) {
            DATA_TYPE_MEDIA -> {
                _networkOrCacheVideoLoadingStartedTimestamp = System.currentTimeMillis()
            }
            DATA_TYPE_MANIFEST -> {
                _networkOrCacheVideoLoadingStartedTimestamp = System.currentTimeMillis()
            }
        }
    }

    internal fun onLoadCompleted(
        loadTime: LoadEventInfo,
        eventTime: AnalyticsListener.EventTime,
        mediaLoadData: MediaLoadData,
    ) {
        when (mediaLoadData.dataType) {
            DATA_TYPE_MEDIA -> {
                _networkOrCacheVideoLoadingDurationMs = loadTime.loadDurationMs
                _networkOrCacheVideoLoadingCompletedTimestamp = System.currentTimeMillis()
            }
            DATA_TYPE_MANIFEST -> {
                _networkOrCacheVideoLoadingDurationMs = loadTime.loadDurationMs
                _networkOrCacheVideoLoadingCompletedTimestamp = System.currentTimeMillis()
            }
        }
    }

    fun onVideoStartedOrFullyReadyToBePlayed(state: Int) {
        if (state == Player.STATE_READY) {
            _videoStartedOrFullyReadyToBePlayedTimestamp = System.currentTimeMillis()
        }
    }

    fun onRenderedFirstFrame(
        eventTime: AnalyticsListener.EventTime,
        output: Any,
        renderTimeMs: Long,
    ) {
        _videoFirstFrameRenderedTimestamp = System.currentTimeMillis()
        Timber.tag(metricTag).d("onRenderedFirstFrame: renderTimeMs=$renderTimeMs")
    }

    internal fun invalidate() {
        _networkOrCacheVideoLoadingStartedTimestamp = DEFAULT_VALUE
        _networkOrCacheVideoLoadingCompletedTimestamp = DEFAULT_VALUE
        _networkOrCacheVideoLoadingDurationMs = 0L
        _videoFirstFrameRenderedTimestamp = DEFAULT_VALUE
        _videoStartedOrFullyReadyToBePlayedTimestamp = DEFAULT_VALUE
    }
}