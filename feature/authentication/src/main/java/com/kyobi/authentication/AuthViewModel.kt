package com.kyobi.authentication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.handleErrors
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.LoggedInUser
import com.kyobi.domain.provider.auth.AuthStateProvider
import com.kyobi.domain.usecase.LoginUseCase
import com.kyobi.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authStateProvider: AuthStateProvider
): ViewModel(), AuthStateProvider by authStateProvider {
    private val _loginState = MutableStateFlow<DomainNetworkResult<LoggedInUser>>(DomainNetworkResult.Loading)
    val loginState: StateFlow<DomainNetworkResult<LoggedInUser>> = _loginState.asStateFlow()

    private val _anonymousLoginState = MutableStateFlow<DomainNetworkResult<LoggedInUser>>(DomainNetworkResult.Loading)
    val anonymousLoginState: StateFlow<DomainNetworkResult<LoggedInUser>> = _anonymousLoginState.asStateFlow()

    init {
        initializeSession()
    }

    private fun initializeSession() {
        viewModelScope.launchOnIO {
            loginUseCase.loginAnonymously()
                .handleErrors {
                    authStateProvider.authUiState.value.copy(
                        isLoading = false,
                        error = it.message)
                }.collect { result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            authStateProvider.updateAuthState(
                                user = null,
                                isAnonymous = true
                            )
                            getLatestCurrentUser()
                        }
                        is DomainNetworkResult.Error -> {
                            authStateProvider.authUiState.value.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
                        is DomainNetworkResult.Loading -> {
                            authStateProvider.authUiState.value.copy(
                                isLoading = true
                            )
                        }
                    }
                    _anonymousLoginState.value = result
                }
        }
    }

    private fun getLatestCurrentUser() {
        viewModelScope.launchOnIO {
            loginUseCase.getCurrentUser()
                .handleErrors {
                    authStateProvider.authUiState.value.copy(
                        isLoading = false,
                        error = it.message)
                }.collect { result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            val isAnonymous = result.data.userType == com.kyobi.domain.model.UserType.ANONYMOUS
                            authStateProvider.updateAuthState(
                                user = result.data,
                                isAnonymous = isAnonymous
                            )
                        }
                        is DomainNetworkResult.Error -> {
                            authStateProvider.authUiState.value.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
                        is DomainNetworkResult.Loading -> {
                            authStateProvider.authUiState.value.copy(
                                isLoading = true
                            )
                        }
                    }
                }
        }
    }

    override fun logout() {
        viewModelScope.launchOnIO {
            logoutUseCase.logout()
                .handleErrors {
                    authStateProvider.authUiState.value.copy(
                        isLoading = false,
                        error = it.message)
                }.collect{ result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            authStateProvider.logout()
                            // Sau khi logout, login anonymously lại
                            initializeSession()
                        }
                        is DomainNetworkResult.Error -> {
                            authStateProvider.authUiState.value.copy(
                                isLoading = false,
                                error = result.exception.message
                            )
                        }
                        is DomainNetworkResult.Loading -> {
                            authStateProvider.authUiState.value.copy(
                                isLoading = true
                            )
                        }
                    }
                }
        }
    }

    /* Sau khi login success, sign up success se call lai ham nay
    * */
    override fun updateAuthState(user: LoggedInUser?, isAnonymous: Boolean) {
        authStateProvider.updateAuthState(user, isAnonymous)
    }
}