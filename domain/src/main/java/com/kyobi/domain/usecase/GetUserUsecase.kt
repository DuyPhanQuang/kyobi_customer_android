package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.LoggedInUser
import kotlinx.coroutines.flow.Flow

interface GetUserUsecase {
    suspend operator fun invoke(): Flow<DomainNetworkResult<LoggedInUser>>
}