package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.LoggedInUser
import kotlinx.coroutines.flow.Flow

interface LoginUseCase {
    suspend fun login(email: String, password: String): Flow<DomainNetworkResult<LoggedInUser>>
    suspend fun loginAnonymously(): Flow<DomainNetworkResult<LoggedInUser>>
    suspend fun getCurrentUser(): Flow<DomainNetworkResult<LoggedInUser>>
}