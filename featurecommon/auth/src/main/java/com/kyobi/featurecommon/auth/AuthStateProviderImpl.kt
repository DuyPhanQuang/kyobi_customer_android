package com.kyobi.featurecommon.auth

import com.kyobi.domain.model.LoggedInUser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthStateProviderImpl @Inject constructor() : AuthStateProvider {
    private val _authUiState = MutableStateFlow(AuthUiState())
    override val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    override fun setLoading(isLoading: Boolean) {
        _authUiState.value = _authUiState.value.copy(isLoading = isLoading)
    }

    override fun setError(error: String?) {
        _authUiState.value = _authUiState.value.copy(
            isLoading = false,
            error = error
        )
    }

    override fun updateAuthState(user: LoggedInUser?, isAnonymous: Boolean) {
        _authUiState.value = _authUiState.value.copy(
            isLoading = false,
            isLoggedIn = user != null,
            isAnonymous = isAnonymous,
            currentUser = user,
            error = null
        )
    }

    override fun logout() {
        _authUiState.value = _authUiState.value.copy(
            isLoading = false,
            isLoggedIn = false,
            isAnonymous = false,
            currentUser = null,
            error = null
        )
    }
}