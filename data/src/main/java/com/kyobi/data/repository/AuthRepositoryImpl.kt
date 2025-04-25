package com.kyobi.data.repository

import com.kyobi.core.storage.TokenStorage
import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.LoggedInUser
import com.kyobi.domain.model.request.LoginRequest
import com.kyobi.domain.model.request.SignupRequest
import com.kyobi.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val apiService: KyobiApiService,
    private val tokenStorage: TokenStorage
): AuthRepository {
    override suspend fun login(request: LoginRequest): LoggedInUser {
        val response = apiService.login(request)
        tokenStorage.saveTokens(response.session.accessToken, response.session.refreshToken)
        return response.toLoggedInUser()
    }

    override suspend fun loginAnonymously(): LoggedInUser {
        val response = apiService.loginAnonymously()
        val session = response.data.session
        tokenStorage.saveTokens(session.accessToken, session.refreshToken)
        return response.toLoggedInUser()
    }

    override suspend fun getAuthUser(): LoggedInUser {
        val response = apiService.getAuthUser()
        return response.toLoggedInUser()
    }

    override suspend fun logout() {
        apiService.logout()
        tokenStorage.clearTokens()
        tokenStorage.clearFcmToken()
    }

    override suspend fun signup(request: SignupRequest): Boolean {
        val response = apiService.signup(request)
        val isSuccess = response.data == null || !response.data.user.isAnonymous
        return isSuccess
    }
}