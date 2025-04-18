package com.kyobi.authentication.signup

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
import com.kyobi.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SignupViewModel @Inject constructor(
    private val signUpUseCase: SignUpUseCase,
    private val authStateProvider: AuthStateProvider
): ViewModel() {

    val authUiState = authStateProvider.authUiState

    var signUpUiState by mutableStateOf(SignUpUiState())
        private set

    fun onPhoneChanged(phone: String) {
        signUpUiState = signUpUiState.copy(phone = phone, error = null)
    }

    fun onEmailChanged(email: String) {
        signUpUiState = signUpUiState.copy(email = email, error = null)
    }

    fun onPasswordChanged(password: String) {
        signUpUiState = signUpUiState.copy(password = password, error = null)
    }

    fun submitSignup() {
        if (signUpUiState.email.isBlank() ||
            signUpUiState.password.isBlank()) {
            signUpUiState = signUpUiState.copy(error = "Vui lòng nhập đầy đủ thông tin")
            return
        }

        viewModelScope.launchOnIO {
            signUpUseCase.signUp(signUpUiState.email, signUpUiState.password, signUpUiState.phone)
                .withLoading {
                    signUpUiState = signUpUiState.copy(
                        isLoading = false,
                        error = null)
                }.handleErrors {
                    signUpUiState = signUpUiState.copy(
                        isLoading = false,
                        error = it.message ?: "Something went wrong")
                }.collect { result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            signUpUiState = signUpUiState.copy(
                                isLoading = false,
                                error = null)
                            authStateProvider.updateAuthState(
                                result.data,
                                isAnonymous = false)
                        }
                        is DomainNetworkResult.Error -> {
                            signUpUiState = signUpUiState.copy(
                                isLoading = false,
                                error = result.exception.message ?: "Something went wrong")
                        }
                        is DomainNetworkResult.Loading -> {
                            signUpUiState = signUpUiState.copy(isLoading = true)
                        }
                    }
                }
        }
    }
}