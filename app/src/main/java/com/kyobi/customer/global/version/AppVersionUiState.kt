package com.kyobi.customer.global.version

data class AppVersionUiState(
    val minimumVersion: String? = null,
    val maximumVersion: String? = null,
    val isMaintenance: Boolean = false,
    val maintenanceMessage: String? = null,
    val forceUpdate: Boolean = false,
    val forceUpdateMessage: String? = null,
    val showForceUpdate: Boolean = false,
    val showUpdateNotification: Boolean = false,
    val updateNotificationMessage: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)