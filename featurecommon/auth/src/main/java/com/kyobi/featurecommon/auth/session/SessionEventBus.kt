package com.kyobi.featurecommon.auth.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionEventBus @Inject constructor() {
    private val tag = "SessionEventBus"
    private val _sessionEvents = MutableSharedFlow<Session?>(replay = 1)
    val sessionEvents: SharedFlow<Session?> = _sessionEvents

    private val _notificationPermissionGranted = MutableSharedFlow<Boolean?>(replay = 1)
    val notificationPermissionGranted: SharedFlow<Boolean?> = _notificationPermissionGranted.asSharedFlow()

    suspend fun emitSession(session: Session?) {
        Timber.tag(tag).d("Emitting session: $session")
        _sessionEvents.emit(session)
    }

    suspend fun emitNotificationPermissionGranted(isGranted: Boolean?) {
        Timber.tag(tag).d("Emitting notification permission granted: $isGranted")
        _notificationPermissionGranted.emit(isGranted)
    }
}