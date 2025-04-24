package com.kyobi.featurecommon.auth.session

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionEventBus @Inject constructor() {
    private val _sessionFlow = MutableSharedFlow<Session?>(replay = 1)
    val sessionFlow: SharedFlow<Session?> = _sessionFlow
    private val tag = "SessionEventBus"

    init {
        Timber.tag(tag).d("instance created: $this")
    }

    suspend fun emitSession(session: Session?) {
        Timber.tag(tag).d("Emitting session to SharedFlow: $session")
        _sessionFlow.emit(session)
    }
}