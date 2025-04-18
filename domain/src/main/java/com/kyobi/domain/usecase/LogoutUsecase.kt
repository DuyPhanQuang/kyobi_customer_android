package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import kotlinx.coroutines.flow.Flow

interface LogoutUseCase {
    fun logout(): Flow<DomainNetworkResult<Unit>>
}