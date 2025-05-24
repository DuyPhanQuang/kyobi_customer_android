package com.kyobi.featurecommon.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.core.storage.TokenStorage
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.LoggedInUser
import com.kyobi.domain.usecase.GetUserUseCase
import com.kyobi.domain.usecase.LoginUseCase
import com.kyobi.domain.usecase.LogoutUseCase
import com.kyobi.featurecommon.auth.session.Session
import com.kyobi.featurecommon.auth.session.SessionEventBus
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val getUserUsecase: GetUserUseCase,
    private val authStateProvider: AuthStateProvider,
    private val tokenStorage: TokenStorage,
    private val sessionEventBus: SessionEventBus,
    private val authEventBus: AuthEventBus
): ViewModel(), AuthStateProvider by authStateProvider {
    private val tag = "AuthViewModel"

    init {
        initializeSession()
        // Subscriber auth eventbus
        viewModelScope.launchOnIO {
            authEventBus.authEvents.collect { event ->
                Timber.tag(tag).d("***authEventBus*** subscribed event: $event")
                when (event) {
                    // sau khi bên login manual viewmodel xử lý submit login success thì sẽ emit
                    // để auth viewmodel xử lý fetch và update user
                    is AuthEvent.LoginSuccess -> {
                        if (event.shouldFetchLatestUser) {
                            getLatestCurrentUser()
                        }
                        updateAuthState(event.user, event.isAnonymous)
                        return@collect
                    }
                }
            }
        }
    }

    // thực hiện login anonymously user
    private fun initializeSession() {
        viewModelScope.launchOnIO {
            // Kiểm tra access token và refresh token
            val accessToken = tokenStorage.getAccessToken()
            val refreshToken = tokenStorage.getRefreshToken()

            if (accessToken != null && refreshToken != null) {
                // Có token, thử lấy user hiện tại
                Timber.tag(tag).d("Found access token, attempting to get current user")
                getLatestCurrentUser()
            } else {
                // Không có token, gọi login anonymously ngay
                Timber.tag(tag).d("No access token found, proceeding with anonymous login")
                performAnonymousLogin()
            }
        }
    }

    private fun performAnonymousLogin() {
        viewModelScope.launchOnIO {
            loginUseCase.loginAnonymously().collect { result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            // Emit Session
                            Timber.tag(tag).d("Login anonymously success")
                            handleEmitSession(Session.fromLoggedInUser(result.data))
                            authStateProvider.updateAuthState(user = result.data, isAnonymous = true)
                        }
                        is DomainNetworkResult.Error -> {
                            when (result) {
                                is DomainNetworkResult.Error.KyobiApi -> {
                                    Timber.tag(tag).e("KyobiApiException: ${result.exception.message}")
                                    authStateProvider.setError(result.exception.message ?: "API error")
                                }
                                is DomainNetworkResult.Error.Generic -> {
                                    Timber.tag(tag).e("Generic error: ${result.throwable.message}")
                                    authStateProvider.setError(result.throwable.message ?: "Unknown error")
                                }
                                is DomainNetworkResult.Error.ShopifyApi -> {}
                            }
                            return@collect
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
            getUserUsecase.invoke().collect { result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            val isAnonymous = result.data.userType == com.kyobi.domain.model.UserType.ANONYMOUS
                            authStateProvider.updateAuthState(user = result.data, isAnonymous = isAnonymous)
                        }
                        is DomainNetworkResult.Error -> {
                            when (result) {
                                is DomainNetworkResult.Error.KyobiApi -> {
                                    Timber.tag(tag).e("KyobiApiException code:${result.exception.code} message:${result.exception.message}")
                                    if (result.exception.code == 401) {
                                        Timber.tag(tag).d("User not authorized, falling back to anonymous login")
                                        performAnonymousLogin()
                                    }
                                }
                                is DomainNetworkResult.Error.Generic -> {
                                    Timber.tag(tag).e("Generic error: ${result.throwable.message}")
                                    authStateProvider.setError(result.throwable.message ?: "Unknown error")
                                }
                                is DomainNetworkResult.Error.ShopifyApi -> {}
                            }
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
            logoutUseCase.logout().collect{ result ->
                    when (result) {
                        is DomainNetworkResult.Success -> {
                            authStateProvider.logout()
                            // Emit Session
                            handleEmitSession(null)
                            // Sau khi logout, login anonymously lại
                            initializeSession()
                        }
                        is DomainNetworkResult.Error -> {
                            when (result) {
                                is DomainNetworkResult.Error.KyobiApi -> {
                                    authStateProvider.setError(result.exception.message ?: "API error")
                                }
                                is DomainNetworkResult.Error.Generic -> {
                                    authStateProvider.setError(result.throwable.message ?: "Unknown error")
                                }
                                is DomainNetworkResult.Error.ShopifyApi -> {}
                            }
                            return@collect
                        }
                        is DomainNetworkResult.Loading -> {
                            authStateProvider.setLoading(true)
                        }
                    }
                }
        }
    }

    private suspend fun handleEmitSession(session: Session?) {
        Timber.tag(tag).d("Emitting session: $session")
        sessionEventBus.emitSession(session)
        Timber.tag(tag).d("Session emitted successfully")
    }

    /* Sau khi login email/password success sẽ call lại hàm này
    * */
    override suspend fun updateAuthState(user: LoggedInUser?, isAnonymous: Boolean) {
        authStateProvider.updateAuthState(user, isAnonymous)
        handleEmitSession(Session.fromLoggedInUser(user))
    }
}