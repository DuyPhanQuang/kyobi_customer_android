package com.kyobi.trend.performance_metrics

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import timber.log.Timber

@OptIn(UnstableApi::class)
class AudioFocusManager(
    context: Context
) : AudioManager.OnAudioFocusChangeListener {
    private val tag = "AudioFocusManager"
    private val audioManager: AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val audioFocusRequest: AudioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        .setOnAudioFocusChangeListener(this)
        .build()

    fun requestAudioFocus() {
        val result = audioManager.requestAudioFocus(audioFocusRequest)
        Timber.tag(tag).d("Audio focus request result: ${result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED}")
    }

    fun abandonAudioFocus() {
        audioManager.abandonAudioFocusRequest(audioFocusRequest)
        Timber.tag(tag).d("Audio focus abandoned")
    }

    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> Timber.tag(tag).d("Audio focus gained")
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT -> Timber.tag(tag).d("Audio focus gained transient")
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE -> Timber.tag(tag).d("Audio focus gained transient exclusive")
            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK -> Timber.tag(tag).d("Audio focus gained transient may duck")
            AudioManager.AUDIOFOCUS_LOSS -> Timber.tag(tag).w("Audio focus lost")
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> Timber.tag(tag).w("Audio focus lost transient")
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> Timber.tag(tag).w("Audio focus lost transient can duck")
        }
    }
}