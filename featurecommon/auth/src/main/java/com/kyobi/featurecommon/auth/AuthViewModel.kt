package com.kyobi.featurecommon.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.handleErrors
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.LoggedInUser
import com.kyobi.domain.usecase.LoginUseCase
import com.kyobi.domain.usecase.LogoutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authStateProvider: AuthStateProvider
): ViewModel(), AuthStateProvider by authStateProvider {
    init {
        initializeSession()
    }

    private fun initializeSession() {
        viewModelScope.launchOnIO {
            loginUseCase.loginAnonymously()
                .handleErrors {
                    authStateProvider.setError(it.message)
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
                            authStateProvider.setError(result.exception.message)
                        }
                        is DomainNetworkResult.Loading -> {
                            authStateProvider.setLoading(true)
                        }
                    }
                }
        }
    }

    private fun getLatestCurrentUser() {
        viewModelScope.launchOnIO {
            loginUseCase.getCurrentUser()
                .handleErrors {
                    authStateProvider.setError(it.message)
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
                            authStateProvider.setError(result.exception.message)
                        }
                        is DomainNetworkResult.Loading -> {
                            authStateProvider.setLoading(true)
                        }
                    }
                }
        }
    }

    override fun logout() {
        viewModelScope.launchOnIO {
            logoutUseCase.logout()
                .handleErrors {
                    authStateProvider.setError(it.message)
                }.collect{ result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            authStateProvider.logout()
                            // Sau khi logout, login anonymously lại
                            initializeSession()
                        }
                        is DomainNetworkResult.Error -> {
                            authStateProvider.setError(result.exception.message)
                        }
                        is DomainNetworkResult.Loading -> {
                            authStateProvider.setLoading(true)
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