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
    if (source != null) {
        if (currentMediaItem?.mediaId != source.mediaItem.mediaId) {
            setMediaSource(source)
            prepare()
        }
        playWhenReady = false
    } else {
        stop()
        clearMediaItems()
        playWhenReady = false
    }
    Timber.tag(TAG).d("pauseForPrev done")
}

@OptIn(UnstableApi::class)
fun ExoPlayer.playForCurrent(isFirstTime: Boolean = false, source: MediaSource?) {
    Timber.tag(TAG).d("playForCurrent: player=$this, mediaItem=${source?.mediaItem?.mediaId}")
    if (source != null && (isFirstTime || currentMediaItem?.mediaId != source.mediaItem.mediaId)) {
        Timber.tag(TAG).d("playForCurrent: setting new MediaSource, isFirstTime=$isFirstTime, mediaId=${source.mediaItem.mediaId}, playbackState=$playbackState")
        setMediaSource(source)
        prepare()
        volume = 1f
        repeatMode = Player.REPEAT_MODE_ONE
        playWhenReady = true
    } else if (currentMediaItem != null && playbackState in listOf(Player.STATE_BUFFERING, Player.STATE_READY)) {
        Timber.tag(TAG).d("playForCurrent: reusing prepared MediaSource, mediaId=${currentMediaItem?.mediaId}, playbackState=$playbackState")
        volume = 1f
        repeatMode = Player.REPEAT_MODE_ONE
        playWhenReady = true
    } else {
        Timber.tag(TAG).d("playForCurrent: no valid MediaSource, stopping playback")
        stop()
        clearMediaItems()
        playWhenReady = false
    }
}

@OptIn(UnstableApi::class)
fun ExoPlayer.prepareForNext(source: MediaSource?) {
    if (source != null && currentMediaItem?.mediaId != source.mediaItem.mediaId) {
        setMediaSource(source)
        prepare()
        Timber.tag(TAG).d("prepareForNext: setMediaSource=${source.mediaItem.mediaId}")
    }
    volume = 0f
    repeatMode = Player.REPEAT_MODE_OFF
    playWhenReady = false
}

@OptIn(UnstableApi::class)
fun ExoPlayer.resetPrevBeforeReuse() {
    stop()
    clearMediaItems()
    playWhenReady = false
    Timber.tag(TAG).d("resetPrevBeforeReuse done")
}

@OptIn(UnstableApi::class)
fun ExoPlayer.resetNextBeforeReuse() {
    stop()
    clearMediaItems()
    playWhenReady = false
    Timber.tag(TAG).d("resetNextBeforeReuse done")
}