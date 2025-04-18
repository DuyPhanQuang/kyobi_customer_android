package com.kyobi.domain.repository

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.domain.model.LoggedInUser
import com.kyobi.domain.model.request.LoginRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): RestNetworkResult<LoggedInUser>
    suspend fun loginAnonymously(): RestNetworkResult<LoggedInUser>
    suspend fun getAuthUser(): RestNetworkResult<LoggedInUser>
}