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
import com.kyobi.home.ui.tab.deals.flip_clock.ui.FlipClockEvents
import kotlinx.coroutines.delay
import kotlin.math.ceil
import kotlin.math.max

@Composable
fun Flipper(
    modifier: Modifier = Modifier
) {
    var endTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var remainingSeconds by remember { mutableIntStateOf(0) }

    fun updateRemainingTime() {
        remainingSeconds = ceil(max(endTime - System.currentTimeMillis(), 0L).toFloat() / 1000F).toInt()
    }

    fun addTime(millis: Long) {
        endTime = max(endTime, System.currentTimeMillis()) + millis
        updateRemainingTime()
    }

    fun addHours(hours: Int) {
        addTime(hours * 3600 * 1000L)
    }

    fun addMinutes(minutes: Int) {
        addTime(minutes * 60 * 1000L)
    }

    fun addSeconds(seconds: Int) {
        addTime(seconds * 1000L)
    }

    LaunchedEffect(Unit) {
        // Mock 12:43:12 bằng cách gọi các hàm add
        addHours(12)
        addMinutes(43)
        addSeconds(12)

        while (true) {
            updateRemainingTime()
            delay(100L)
        }
    }

    FlipClock(
        seconds = remainingSeconds,
        endMillis = endTime,
        events = FlipClockEvents(
            onHoursIncrement = { addHours(1) },
            onHoursDecrement = { addHours(-1) },
            onMinutesIncrement = { addMinutes(1) },
            onMinutesDecrement = { addMinutes(-1) },
            onSecondsIncrement = { addSeconds(1) },
            onSecondsDecrement = { addSeconds(-1) }
        )
    )
}