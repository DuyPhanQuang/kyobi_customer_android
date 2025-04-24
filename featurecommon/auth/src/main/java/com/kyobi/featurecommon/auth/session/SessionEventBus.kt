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
    private val _sessionFlow = MutableSharedFlow<Session?>(replay = 1)
    val sessionFlow: SharedFlow<Session?> = _sessionFlow

    private val _notificationPermissionGranted = MutableSharedFlow<Boolean?>(replay = 1)
    val notificationPermissionGranted: SharedFlow<Boolean?> = _notificationPermissionGranted.asSharedFlow()

    init {
        Timber.tag(tag).d("instance created: $this")
    }

    suspend fun emitSession(session: Session?) {
        Timber.tag(tag).d("Emitting session to SharedFlow: $session")
        _sessionFlow.emit(session)
    }

    suspend fun emitNotificationPermissionGranted(isGranted: Boolean?) {
        Timber.tag(tag).d("Emitting notification permission granted to SharedFlow: $isGranted")
        _notificationPermissionGranted.emit(isGranted)
    }
}