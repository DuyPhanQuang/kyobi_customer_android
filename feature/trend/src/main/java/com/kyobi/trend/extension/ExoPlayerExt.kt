package com.kyobi.trend.extension

import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import timber.log.Timber

private const val TAG = "ExoPlayer"

@OptIn(UnstableApi::class)
fun ExoPlayer.pauseForPrev(source: MediaSource?) {
    Timber.tag(TAG).d("pauseForPrev done")
    stop()
    if (source != null) {
        clearMediaItems()
        setMediaSource(source)
        prepare()
    }
}

@OptIn(UnstableApi::class)
fun ExoPlayer.playForCurrent(isFirstTime: Boolean, source: MediaSource?) {
    Timber.tag(TAG).d("playForCurrent: player=$this, mediaItem=${source?.mediaItem?.mediaId}")
    if (source == null) {
        Timber.tag(TAG).w("source null")
        return
    }
    clearMediaItems()
    setMediaSource(source)
    if (playbackState == Player.STATE_IDLE || isFirstTime) {
        Timber.tag(TAG).d("playForCurrent: prepare called, isFirstTime=$isFirstTime, playbackState=$playbackState")
        prepare()
    }
    playWhenReady = true
}

@OptIn(UnstableApi::class)
fun ExoPlayer.prepareForNext(source: MediaSource?) {
    Timber.tag(TAG).d("prepareForNext: setMediaSource=${source?.mediaItem?.mediaId}")
    stop()
    clearMediaItems()
    if (source != null) {
        setMediaSource(source)
        prepare()
    }
}

@OptIn(UnstableApi::class)
fun ExoPlayer.resetPrevBeforeReuse() {
    Timber.tag(TAG).d("resetPrevBeforeReuse: player=$this")
    stop()
    clearMediaItems()
}

@OptIn(UnstableApi::class)
fun ExoPlayer.resetNextBeforeReuse() {
    Timber.tag(TAG).d("resetNextBeforeReuse: player=$this")
    stop()
    clearMediaItems()
}