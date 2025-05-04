package com.kyobi.trend.ui

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.upstream.DefaultAllocator
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import timber.log.Timber
import androidx.compose.runtime.Composable

@UnstableApi
class PlayerPool(context: Context) {
    private val tag = "PlayerPool"

    var prevPlayer: ExoPlayer
    var currentPlayer: ExoPlayer
    var nextPlayer: ExoPlayer

    var prevPlayerView: PlayerView
    var currentPlayerView: PlayerView
    var nextPlayerView: PlayerView

    init {
        val prevLoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(2000, 4000, 500, 2000)
            .setPrioritizeTimeOverSizeThresholds(true).build()
        val currentLoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(2000, 4000, 500, 2000)
            .setPrioritizeTimeOverSizeThresholds(true).build()
        val nextLoadControl = DefaultLoadControl.Builder()
            .setAllocator(DefaultAllocator(true, 16))
            .setBufferDurationsMs(2000, 4000, 500, 2000)
            .setPrioritizeTimeOverSizeThresholds(true).build()

        prevPlayer = ExoPlayer.Builder(context).setLoadControl(prevLoadControl).build()
        currentPlayer = ExoPlayer.Builder(context).setLoadControl(currentLoadControl).build()
        nextPlayer = ExoPlayer.Builder(context).setLoadControl(nextLoadControl).build()

        prevPlayerView = createPlayerView(context, prevPlayer)
        currentPlayerView = createPlayerView(context, currentPlayer)
        nextPlayerView = createPlayerView(context, nextPlayer)

        Timber.tag(tag).d("PlayerPool initialized with 3 players and views")
    }

    private fun createPlayerView(context: Context, player: ExoPlayer): PlayerView {
        return PlayerView(context).apply {
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            setKeepContentOnPlayerReset(true)
            setEnableComposeSurfaceSyncWorkaround(true)
            setBackgroundColor(Color.TRANSPARENT)
            setShowBuffering(PlayerView.SHOW_BUFFERING_NEVER)
            keepScreenOn = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            this.player = player
        }
    }

    fun releaseAll() {
        prevPlayer.release()
        currentPlayer.release()
        nextPlayer.release()
        Timber.tag(tag).d("PlayerPool released")
    }

    @Composable
    fun getPlayerViewForPosition(position: Int, currentPosition: Int): PlayerView? {
        return when (position) {
            currentPosition -> currentPlayerView
            currentPosition + 1 -> nextPlayerView
            currentPosition - 1 -> prevPlayerView
            else -> null
        }
    }
}