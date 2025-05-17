package com.kyobi.trend.performance_metrics

import androidx.annotation.OptIn
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi

@OptIn(UnstableApi::class)
open class VideoInfo {
    open val videoWidth: Int
        get() = _videoWidth
    open val videoHeight: Int
        get() = _videoHeight
    open val videoMimeType: String
        get() = _videoMimeType

    private var _videoWidth: Int = 0
    private var _videoHeight: Int = 0
    private var _videoMimeType: String = ""

    fun onTracksChanged(eventTime: AnalyticsListener.EventTime, tracks: Tracks) {
        tracks.groups.forEach { group ->
            for (i in 0 until group.length) {
                val trackFormat = group.getTrackFormat(i)
                if (trackFormat.sampleMimeType?.startsWith("video/") == true) {
                    _videoWidth = trackFormat.width
                    _videoHeight = trackFormat.height
                    _videoMimeType = trackFormat.sampleMimeType ?: ""
                }
            }
        }
    }

    fun invalidate() {
        _videoWidth = 0
        _videoHeight = 0
        _videoMimeType = ""
    }
}