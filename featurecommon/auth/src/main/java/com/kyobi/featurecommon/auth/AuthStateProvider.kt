package com.kyobi.featurecommon.auth

import com.kyobi.domain.model.LoggedInUser
import kotlinx.coroutines.flow.StateFlow

interface AuthStateProvider {
    val authUiState: StateFlow<AuthUiState>
    fun setLoading(isLoading: Boolean)
    fun setError(error: String?)
    suspend fun updateAuthState(user: LoggedInUser?, isAnonymous: Boolean)
    fun logout()
}