package com.kyobi.domain.extension

import com.kyobi.domain.model.FlashSaleInfo
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun FlashSaleInfo.toDeviceTimeZone(): FlashSaleInfo {
    val deviceZoneId = ZoneId.systemDefault() // Lấy múi giờ của thiết bị
    val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME // Định dạng ISO 8601
    return this.copy(
        startTime = this.startTime?.let { time ->
            val utcDateTime = ZonedDateTime.parse(time, formatter)
            val deviceDateTime = utcDateTime.withZoneSameInstant(deviceZoneId)
            deviceDateTime.format(formatter)
        },
        endTime = this.endTime?.let { time ->
            val utcDateTime = ZonedDateTime.parse(time, formatter)
            val deviceDateTime = utcDateTime.withZoneSameInstant(deviceZoneId)
            deviceDateTime.format(formatter)
        }
    )
}