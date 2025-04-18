package com.kyobi.authentication.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.handleErrors
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.core.coroutines.withLoading
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.provider.auth.AuthStateProvider
import com.kyobi.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val authStateProvider: AuthStateProvider
): ViewModel() {

    val authUiState = authStateProvider.authUiState

    var loginUiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChanged(email: String) {
        loginUiState = loginUiState.copy(email = email, error = null)
    }

    fun onPasswordChanged(password: String) {
        loginUiState = loginUiState.copy(password = password, error = null)
    }

    fun submitLogin() {
        if (loginUiState.email.isBlank() ||
            loginUiState.password.isBlank()) {
            loginUiState = loginUiState.copy(error = "Vui lòng nhập email và mật khẩu")
            return
        }
        viewModelScope.launchOnIO {
            loginUseCase.login(loginUiState.email, loginUiState.password)
                .withLoading {
                    loginUiState = loginUiState.copy(
                        isLoading = true,
                        error = null)
                }.handleErrors {
                    loginUiState = loginUiState.copy(
                        isLoading = false,
                        error = it.message ?: "Something went wrong")
                }.collect { result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            loginUiState = loginUiState.copy(
                                isLoading = false,
                                error = null)
                            authStateProvider.updateAuthState(
                                result.data,
                                isAnonymous = false)
                        }
                        is DomainNetworkResult.Error -> {
                            loginUiState = loginUiState.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Something went wrong"
                            )
                        }
                        is DomainNetworkResult.Loading -> {
                            loginUiState = loginUiState.copy(isLoading = true)
                        }
                    }
                }
        }
    }
}