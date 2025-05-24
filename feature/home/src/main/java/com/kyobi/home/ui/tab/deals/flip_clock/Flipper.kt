package com.kyobi.home.ui.tab.deals.flip_clock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.kyobi.home.ui.tab.deals.flip_clock.ui.FlipClock
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun Flipper(
    modifier: Modifier = Modifier,
    endTime: String
) {
    var endTimeMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var remainingSeconds by remember { mutableIntStateOf(0) }

    fun updateRemainingTime() {
        remainingSeconds = ceil(max(endTimeMillis  - System.currentTimeMillis(), 0L).toFloat() / 1000F).toInt()
    }

    fun addTime(millis: Long) {
        endTimeMillis  = max(endTimeMillis, System.currentTimeMillis()) + millis
        updateRemainingTime()
    }

    fun addHours(hours: Int) = addTime(hours * 3600 * 1000L)
    fun addMinutes(minutes: Int) = addTime(minutes * 60 * 1000L)
    fun addSeconds(seconds: Int) = addTime(seconds * 1000L)

    LaunchedEffect(Unit) {
        val formatter = DateTimeFormatter.ISO_DATE_TIME
        val endInstant = Instant.from(formatter.parse(endTime))
        val now = Instant.now()
        val remainingMillis = Duration.between(now, endInstant).toMillis().coerceAtLeast(0)
        val totalSeconds = (remainingMillis / 1000).toInt()

        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        addHours(hours)
        addMinutes(minutes)
        addSeconds(seconds)

        while (true) {
            updateRemainingTime()
            delay(100L)
        }
    }

    FlipClock(
        modifier = modifier,
        seconds = remainingSeconds,
        endMillis = endTimeMillis,
    )
}