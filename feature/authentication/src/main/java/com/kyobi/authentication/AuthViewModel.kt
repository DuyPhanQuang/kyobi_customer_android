package com.kyobi.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.handleErrors
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.LoggedInUser
import com.kyobi.domain.provider.auth.AuthStateProvider
import com.kyobi.domain.provider.auth.AuthUiState
import com.kyobi.domain.usecase.LoginUseCase
import com.kyobi.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase
): ViewModel(), AuthStateProvider {
    private val _authUiState = MutableStateFlow(AuthUiState())
    override val authUiState = _authUiState.asStateFlow()

    init {
        initializeSession()
    }

    private fun initializeSession() {
        viewModelScope.launchOnIO {
            loginUseCase.loginAnonymously()
                .handleErrors {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        error = it.message)
                }.collect { result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            _authUiState.value = _authUiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                isAnonymous = true,
                                currentUser = result.data,
                                error = null)
                        }
                        is DomainNetworkResult.Error -> {
                            _authUiState.value = _authUiState.value.copy(
                                isLoading = false,
                                error = result.exception.message)
                        }
                        is DomainNetworkResult.Loading -> {
                            _authUiState.value = _authUiState.value.copy(
                                isLoading = true)
                        }
                    }
                }
        }
        checkCurrentUser()
    }

    private fun checkCurrentUser() {
        viewModelScope.launchOnIO {
            loginUseCase.getCurrentUser()
                .handleErrors {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false,
                        error = null)
                }.collect { result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            val isAnonymous = result.data.userType == com.kyobi.domain.model.UserType.ANONYMOUS
                            _authUiState.value = _authUiState.value.copy(
                                isLoading = false,
                                isLoggedIn = true,
                                isAnonymous = isAnonymous,
                                currentUser = result.data,
                                error = null)
                        }
                        is DomainNetworkResult.Error -> {
                            _authUiState.value = _authUiState.value.copy(
                                isLoading = false,
                                error = result.exception.message)
                        }
                        is DomainNetworkResult.Loading -> {
                            _authUiState.value = _authUiState.value.copy(
                                isLoading = false)
                        }
                    }
                }
        }
    }

    override fun logout() {
        viewModelScope.launchOnIO {
            logoutUseCase.logout()
                .handleErrors {
                    _authUiState.value = _authUiState.value.copy(
                        isLoading = false, error = it.message)
                }.collect{ result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            _authUiState.value = _authUiState.value.copy(
                                isLoading = false,
                                isLoggedIn = false,
                                isAnonymous = false,
                                currentUser = null,
                                error = null
                            )
                            // login anonymously tro lai sau khi logout
                            initializeSession()
                        }
                        is DomainNetworkResult.Error -> {
                            _authUiState.value = _authUiState.value.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
                        is DomainNetworkResult.Loading -> {
                            _authUiState.value = _authUiState.value.copy(
                                isLoading = true)
                        }
                    }
                }
        }
    }

    /* Sau khi login success, sign up success se call lai ham nay
    * */
    override fun updateAuthState(user: LoggedInUser, isAnonymous: Boolean) {
        _authUiState.value = _authUiState.value.copy(
            isLoading = false,
            isLoggedIn = true,
            isAnonymous = isAnonymous,
            currentUser = user,
            error = null
        )
    }
}