package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.LoggedInUser
import kotlinx.coroutines.flow.Flow

interface SignUpUseCase {
    suspend fun signUp(email: String, password: String, phone: String?): Flow<DomainNetworkResult<LoggedInUser>>
}