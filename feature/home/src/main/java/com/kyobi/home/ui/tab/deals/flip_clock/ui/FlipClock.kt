package com.kyobi.home.ui.tab.deals.flip_clock.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.kyobi.home.ui.tab.deals.flip_clock.getTimeParts
import com.kyobi.theme.headingLg
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphRegularXs
import kotlin.math.ceil

@Composable
fun FlipClock(
    seconds: Int,
    endMillis: Long,
    events: FlipClockEvents
) {
    val animatedSeconds by animateFloatAsState(key = endMillis, targetValue = seconds.toFloat())

    val currentSeconds = ceil(animatedSeconds).toInt()
    val nextSeconds = currentSeconds - 1
    val factor = currentSeconds.toFloat() - animatedSeconds
    val currentParts = getTimeParts(currentSeconds)
    val nextParts = getTimeParts(nextSeconds)

    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp4),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Ends in:",
            color = MaterialTheme.kyobiTheme.colors.onPrimary,
            style = MaterialTheme.kyobiTheme.typography.paragraphRegularXs
        )
        FlapSection(
            currentValue = currentParts.hours,
            nextValue = nextParts.hours,
            factor = if (currentParts.hours == nextParts.hours) 0F else factor
        )
        Text(
            text = ":",
            color = MaterialTheme.kyobiTheme.colors.onPrimary,
            style = MaterialTheme.kyobiTheme.typography.headingLg
        )
        FlapSection(
            currentValue = currentParts.minutes,
            nextValue = nextParts.minutes,
            factor = if (currentParts.minutes == nextParts.minutes) 0F else factor
        )
        Text(
            text = ":",
            color = MaterialTheme.kyobiTheme.colors.onPrimary,
            style = MaterialTheme.kyobiTheme.typography.headingLg
        )
        FlapSection(
            currentValue = currentParts.seconds,
            nextValue = nextParts.seconds,
            factor = if (currentParts.seconds == nextParts.seconds) 0F else factor
        )
    }
}

data class FlipClockEvents(
    val onHoursIncrement: () -> Unit,
    val onHoursDecrement: () -> Unit,
    val onMinutesIncrement: () -> Unit,
    val onMinutesDecrement: () -> Unit,
    val onSecondsIncrement: () -> Unit,
    val onSecondsDecrement: () -> Unit
)