package com.kyobi.domain.provider.auth

import com.kyobi.domain.model.LoggedInUser
import kotlinx.coroutines.flow.StateFlow

interface AuthStateProvider {
    val authUiState: StateFlow<AuthUiState>
    fun logout()
    fun updateAuthState(user: LoggedInUser, isAnonymous: Boolean)
}