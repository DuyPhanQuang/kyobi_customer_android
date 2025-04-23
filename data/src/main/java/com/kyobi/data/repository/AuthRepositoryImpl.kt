package com.kyobi.data.repository

import com.kyobi.core.exceptions.KyobiApiException
import com.kyobi.core.model.RestNetworkResult
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
    override suspend fun login(request: LoginRequest): RestNetworkResult<LoggedInUser> {
        return try {
            val response = apiService.login(request)
            tokenStorage.saveTokens(response.session.accessToken, response.session.refreshToken)
            RestNetworkResult.Success(response.toLoggedInUser())
        } catch (e: KyobiApiException) {
            RestNetworkResult.Error(e.message ?: "Unknown error", e.code)
        }
    }

    override suspend fun loginAnonymously(): RestNetworkResult<LoggedInUser> {
        return try {
            val response = apiService.loginAnonymously()
            val session = response.data.session
            tokenStorage.saveTokens(session.accessToken, session.refreshToken)
            RestNetworkResult.Success(response.toLoggedInUser())
        } catch (e: KyobiApiException) {
            RestNetworkResult.Error(e.message ?: "Unknown error", e.code)
        }
    }

    override suspend fun getAuthUser(): RestNetworkResult<LoggedInUser> {
        return try {
            val response = apiService.getAuthUser()
            RestNetworkResult.Success(response.toLoggedInUser())
        } catch (e: KyobiApiException) {
            RestNetworkResult.Error(e.message ?: "Unknown error", e.code)
        }
    }

    override suspend fun logout(): RestNetworkResult<Unit> {
        return try {
            apiService.logout()
            RestNetworkResult.Success(Unit)
        } catch (e: KyobiApiException) {
            RestNetworkResult.Error(e.message ?: "Unknown error", e.code)
        }
    }

    override suspend fun signup(request: SignupRequest): RestNetworkResult<Boolean> {
        return try {
            val response = apiService.signup(request)
            val isSuccess = response.data == null || !response.data.user.isAnonymous
            RestNetworkResult.Success(isSuccess)
        } catch (e: KyobiApiException) {
            RestNetworkResult.Error(e.message ?: "Unknown error", e.code)
        }
    }
}