package com.kyobi.domain.model

data class AppVersion(
    val isMaintenance: Boolean,
    val maintenanceMessage: String?,
    val minimumVersion: String,
    val maximumVersion: String,
    val forceUpdate: Boolean,
    val forceUpdateMessage: String?,
)