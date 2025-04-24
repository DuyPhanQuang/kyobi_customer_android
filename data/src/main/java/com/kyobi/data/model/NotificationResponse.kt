package com.kyobi.data.model

import com.kyobi.domain.model.Notification
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NotificationResponse(
    @Json(name = "message") val message: String
) {
    fun toNotification(): Notification {
        return Notification(
            message = message
        )
    }
}