package com.kyobi.data.repository

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.core.storage.TokenStorage
import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.LoggedInUser
import com.kyobi.domain.model.UserInfo
import com.kyobi.domain.model.UserType
import com.kyobi.domain.model.request.LoginRequest
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
                val userType = if (response.user.isAnonymous) UserType.ANONYMOUS else UserType.LOGGED_IN
                val phone = response.user.userMetadata?.phone
                RestNetworkResult.Success(
                    LoggedInUser(
                        id = response.user.id,
                        userType = userType,
                        info = UserInfo(
                            email = response.user.email,
                            phoneNumber = phone,
                            nickname = null
                        )
                    )
                )
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
                RestNetworkResult.Success(
                    LoggedInUser(
                        id = response.data.user.id,
                        userType = UserType.ANONYMOUS,
                        info = null
                    )
                )
            }
            is RestNetworkResult.Error -> RestNetworkResult.Error(result.message, result.code)
            is RestNetworkResult.Loading -> RestNetworkResult.Loading
        }
    }

    override suspend fun getAuthUser(): RestNetworkResult<LoggedInUser> {
        return when (val result = apiService.getAuthUser()) {
            is RestNetworkResult.Success -> {
                val response = result.data
                val userType = if (response.isAnonymous) UserType.ANONYMOUS else UserType.LOGGED_IN
                val phone = response.userMetadata?.phone
                RestNetworkResult.Success(
                    LoggedInUser(
                        id = response.id,
                        userType = userType,
                        info = UserInfo(
                            email = response.email,
                            phoneNumber = phone,
                            nickname = null
                        )
                    )
                )
            }
            is RestNetworkResult.Error -> RestNetworkResult.Error(result.message, result.code)
            is RestNetworkResult.Loading -> RestNetworkResult.Loading
        }
    }
}