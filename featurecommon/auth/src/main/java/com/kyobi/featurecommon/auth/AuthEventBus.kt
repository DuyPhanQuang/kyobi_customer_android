package com.kyobi.featurecommon.auth

import com.kyobi.domain.model.LoggedInUser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class AuthEvent {
    data class LoginSuccess(
        val user: LoggedInUser,
        val isAnonymous: Boolean,
        val shouldFetchLatestUser: Boolean = true,
    ) : AuthEvent()
}

@Singleton
class AuthEventBus @Inject constructor() {
    private val _events = MutableSharedFlow<AuthEvent>(replay = 0)
    val events: SharedFlow<AuthEvent> = _events

    suspend fun emitEvent(event: AuthEvent) {
        _events.emit(event)
    }
}