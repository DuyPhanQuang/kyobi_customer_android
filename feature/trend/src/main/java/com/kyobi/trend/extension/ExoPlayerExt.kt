package com.kyobi.trend.extension

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import com.kyobi.trend.ui.PlayerPool
import timber.log.Timber

private val tag = "ExoPlayer"

// only for pp.currentPlayer
@UnstableApi
fun ExoPlayer.playForCurrent(isFirstTime: Boolean = false, forward: Boolean, source: MediaSource?) {
    try {
        source?.let {
            if (isFirstTime) {
                setMediaSource(it, true)
                setPriority(C.PRIORITY_PLAYBACK)
                prepare()
                Timber.tag(tag).d("case first time")
            } else {
                setPriority(C.PRIORITY_PLAYBACK)
                if (forward) {
                    // Đảm bảo media source đã được set từ trước (preload)
                    // chỉ nên thực hiện if này ví lý do nào đó lần gần nhất nextPlayer/prevPlayer prepare thất bại
                    if (playbackState == Player.STATE_IDLE && !isPlaying) {
                        Timber.tag(tag).d("case forward preparing")
                        prepare()
                    }
                    Timber.tag(tag).d("case forward")
                } else {
                    seekTo(0)
                    Timber.tag(tag).d("case backward")
                }
            }
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ONE
            volume = 1f
        }
    }  catch (e: Exception) {
        Timber.tag(tag).e(e, "Error playing current player")
    }
}

// only for pp.nextPlayer
@UnstableApi
fun ExoPlayer.prepareForNext(source: MediaSource?) {
    try {
        source?.let {
            setMediaSource(it, true)
            setPriority(C.PRIORITY_PLAYBACK_PRELOAD)
            repeatMode = Player.REPEAT_MODE_OFF
            volume = 0f
            val startTime = System.nanoTime()
            prepare()
            playWhenReady = false
            addListener(object : Player.Listener {
                override fun onRenderedFirstFrame() {
                    val renderTimeMs = (System.nanoTime() - startTime) / 1_000_000
                    Timber.tag(tag).d("First frame rendered in $renderTimeMs ms")
                    removeListener(this)
                }
                override fun onSurfaceSizeChanged(width: Int, height: Int) {
                    Timber.tag(tag).d("Surface size changed: $width x $height")
                    removeListener(this)
                }
            })
        }
    } catch (e: Exception) {
        Timber.tag(tag).e(e, "Error preparing next player")
    }
}

// only for pp.prevPlayer
@UnstableApi
fun ExoPlayer.pauseForPrev(source: MediaSource?) {
    try {
        source?.let {
            pause()
            setPriority(C.PRIORITY_PLAYBACK_PRELOAD)
            playWhenReady = false
            repeatMode = Player.REPEAT_MODE_OFF
            volume = 0f
            Timber.tag(tag).d("pauseForPrev done")
        }
    } catch (e: Exception) {
        Timber.tag(tag).e(e, "Error pausing prev player")
    }
}

// only for case forward - input: pp.prevPlayer
@UnstableApi
fun ExoPlayer.resetPrevBeforeReuse(pp: PlayerPool) {
    stop()
    clearMediaItems()
//    pp.surfaceHolders[this]?.let { holder ->
//        clearVideoSurfaceHolder(holder)
//        Timber.tag(tag).d("Cleared old SurfaceHolder for prevPlayer: %s", this)
//    }
//    pp.surfaceHolders.remove(this)
    playWhenReady = false
    repeatMode = Player.REPEAT_MODE_OFF
    volume = 0f
    Timber.tag(tag).d("Cleared prevPlayer: %s", this)
}

// only for case backward - input: pp.nextPlayer
@UnstableApi
fun ExoPlayer.resetNextBeforeReuse(pp: PlayerPool) {
    stop()
    clearMediaItems()
    pp.surfaceHolders[this]?.let { holder ->
        clearVideoSurfaceHolder(holder)
        Timber.tag(tag).d("Cleared old SurfaceHolder for nextPlayer: %s", this)
    }
    pp.surfaceHolders.remove(this)
    playWhenReady = false
    repeatMode = Player.REPEAT_MODE_OFF
    volume = 0f
    Timber.tag(tag).d("Cleared nextPlayer: %s", this)
}
