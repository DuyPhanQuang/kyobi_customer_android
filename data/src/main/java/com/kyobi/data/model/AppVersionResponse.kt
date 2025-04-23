package com.kyobi.data.model

import com.kyobi.domain.model.AppVersion
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AppVersionResponse(
    @Json(name = "is_maintenance") val isMaintenance: Boolean,
    @Json(name = "maintenance_message") val maintenanceMessage: String?,
    @Json(name = "minimum_version") val minimumVersion: String,
    @Json(name = "maximum_version") val maximumVersion: String,
    @Json(name = "force_update") val forceUpdate: Boolean,
    @Json(name = "force_update_message") val forceUpdateMessage: String?,
    @Json(name = "updated_at") val updatedAt: String
) {
    fun toAppVersion(): AppVersion {
        return AppVersion(
            isMaintenance = isMaintenance,
            maintenanceMessage = maintenanceMessage,
            minimumVersion = minimumVersion,
            maximumVersion = maximumVersion,
            forceUpdate = forceUpdate,
            forceUpdateMessage = forceUpdateMessage,
        )
    }
}