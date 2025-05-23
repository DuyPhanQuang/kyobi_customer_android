package com.kyobi.domain.usecase

import com.kyobi.domain.model.CateFilter
import com.kyobi.domain.model.DomainNetworkResult
import kotlinx.coroutines.flow.Flow

interface GetFilterSetUseCase {
    suspend fun getFilterSetByCateHandle(handle: String): Flow<DomainNetworkResult<CateFilter?>>

    suspend fun getFilterSetByDefault(): Flow<DomainNetworkResult<CateFilter?>>
}