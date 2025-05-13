package com.kyobi.home.ui.tab.deals.flip_clock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.composable.space.XxsSpaceY
import com.kyobi.home.ui.tab.deals.flip_clock.getTimeParts
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphMd
import kotlin.math.ceil

@Composable
fun FlipClock(
    seconds: Int,
    endMillis: Long,
) {
    val animatedSeconds by animateFloatAsState(key = endMillis, targetValue = seconds.toFloat())

    val currentSeconds = ceil(animatedSeconds).toInt()
    val nextSeconds = currentSeconds - 1
    val factor = currentSeconds.toFloat() - animatedSeconds
    val currentParts = getTimeParts(currentSeconds)
    val nextParts = getTimeParts(nextSeconds)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Ends in",
            color = MaterialTheme.kyobiTheme.colors.onPrimary,
            style = MaterialTheme.kyobiTheme.typography.paragraphMd,
        )
        XxsSpaceY()
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            FlapSection(
                currentValue = currentParts.hours,
                nextValue = nextParts.hours,
                factor = if (currentParts.hours == nextParts.hours) 0F else factor,
                label = "Hours",
            )
            XsSpaceX()
            FlapSection(
                currentValue = currentParts.minutes,
                nextValue = nextParts.minutes,
                factor = if (currentParts.minutes == nextParts.minutes) 0F else factor,
                label = "Minutes",
            )
            XxsSpaceX()
            FlapSection(
                currentValue = currentParts.seconds,
                nextValue = nextParts.seconds,
                factor = if (currentParts.seconds == nextParts.seconds) 0F else factor,
                label = "Seconds",
            )
        }
    }
}