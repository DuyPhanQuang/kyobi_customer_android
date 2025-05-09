package com.kyobi.home.ui.tab.deals.flip_clock

fun getTimeParts(seconds: Int): TimeParts {
    val partHours = seconds / 3600
    val partMinutes = (seconds % 3600) / 60
    val partSeconds = seconds % 60
    return TimeParts(
        hours = partHours,
        minutes = partMinutes,
        seconds = partSeconds
    )
}

data class TimeParts(
    val hours: Int,
    val minutes: Int,
    val seconds: Int
)