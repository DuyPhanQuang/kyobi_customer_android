package com.kyobi.domain.usecase

import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.DomainNetworkResult
import kotlinx.coroutines.flow.Flow

interface GetBannersUseCase {
    suspend operator fun invoke(): Flow<DomainNetworkResult<List<Banner>>>
}