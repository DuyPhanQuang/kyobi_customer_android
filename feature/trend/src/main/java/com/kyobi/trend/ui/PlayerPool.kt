package com.kyobi.trend.ui

import android.content.Context
import android.graphics.Color
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.PlayerView.SHOW_BUFFERING_NEVER
import timber.log.Timber

@UnstableApi
class PlayerPool(ctx: Context) {
    var prevPlayer: ExoPlayer = ExoPlayer.Builder(ctx).setLoadControl(
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(1000, 5000, 500, 1000)
            .build()).build()
    var currentPlayer: ExoPlayer = ExoPlayer.Builder(ctx).setLoadControl(
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(1000, 5000, 500, 1000)
            .build()).build()
    var nextPlayer: ExoPlayer = ExoPlayer.Builder(ctx).setLoadControl(
        DefaultLoadControl.Builder()
            .setBufferDurationsMs(1000, 5000, 500, 1000)
            .build()).build()

    var prevPlayerView: PlayerView = createPlayerView(ctx, prevPlayer)
    var currentPlayerView: PlayerView = createPlayerView(ctx, currentPlayer)
    var nextPlayerView: PlayerView = createPlayerView(ctx, nextPlayer)
    val tag = "PlayerPool"

    @OptIn(UnstableApi::class)
    private fun createPlayerView(context: Context, player: ExoPlayer): PlayerView {
        val playerView = PlayerView(context).apply {
            useController = false
            resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
            setKeepContentOnPlayerReset(true)
            setBackgroundColor(Color.TRANSPARENT)
            setEnableComposeSurfaceSyncWorkaround(true)
            setShowBuffering(SHOW_BUFFERING_NEVER)
            keepScreenOn = true
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        }
        playerView.player = player
        return playerView
    }

    fun releaseAll() {
        prevPlayer.release()
        currentPlayer.release()
        nextPlayer.release()
    }
}