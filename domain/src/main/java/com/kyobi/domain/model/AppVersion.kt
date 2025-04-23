package com.kyobi.domain.model

data class AppVersion(
    val isMaintenance: Boolean,
    val maintenanceMessage: String?,
    val minimumVersion: String,
    val maximumVersion: String, // version mới nhất đã indexed trên playstore
    val forceUpdate: Boolean,
    val forceUpdateMessage: String?,
)