package com.kyobi.data.repository

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
        return when (val result = apiService.login(request)) {
            is RestNetworkResult.Success -> {
                val response = result.data
                tokenStorage.saveTokens(response.session.accessToken, response.session.refreshToken)
                RestNetworkResult.Success(response.toLoggedInUser())
            }
            is RestNetworkResult.Error -> RestNetworkResult.Error(result.message, result.code)
            is RestNetworkResult.Loading -> RestNetworkResult.Loading
        }
    }

    override suspend fun loginAnonymously(): RestNetworkResult<LoggedInUser> {
        return when (val result = apiService.loginAnonymously()) {
            is RestNetworkResult.Success -> {
                val response = result.data
                val session = response.data.session
                tokenStorage.saveTokens(session.accessToken, session.refreshToken)
                RestNetworkResult.Success(response.toLoggedInUser())
            }
            is RestNetworkResult.Error -> RestNetworkResult.Error(result.message, result.code)
            is RestNetworkResult.Loading -> RestNetworkResult.Loading
        }
    }

    override suspend fun getAuthUser(): RestNetworkResult<LoggedInUser> {
        return when (val result = apiService.getAuthUser()) {
            is RestNetworkResult.Success -> {
                val response = result.data
                RestNetworkResult.Success(response.toLoggedInUser())
            }
            is RestNetworkResult.Error -> RestNetworkResult.Error(result.message, result.code)
            is RestNetworkResult.Loading -> RestNetworkResult.Loading
        }
    }

    override suspend fun logout(): RestNetworkResult<Unit> {
        return when (val result = apiService.logout()) {
            is RestNetworkResult.Success -> {
                tokenStorage.clearTokens()
                RestNetworkResult.Success(Unit)
            }
            is RestNetworkResult.Error -> RestNetworkResult.Error(result.message, result.code)
            is RestNetworkResult.Loading -> RestNetworkResult.Loading
        }
    }

    override suspend fun signup(request: SignupRequest): RestNetworkResult<Boolean> {
        return when (val result = apiService.signup(request)) {
            is RestNetworkResult.Success -> {
                val response = result.data
                val isSuccess = response.data == null || !response.data.user.isAnonymous
                RestNetworkResult.Success(isSuccess)
            }
            is RestNetworkResult.Error -> RestNetworkResult.Error(result.message, result.code)
            is RestNetworkResult.Loading -> RestNetworkResult.Loading
        }
    }
}