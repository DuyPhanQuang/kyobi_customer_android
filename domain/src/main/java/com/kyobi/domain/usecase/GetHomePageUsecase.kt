package com.kyobi.domain.usecase

import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.TopCatalog
import kotlinx.coroutines.flow.Flow

interface GetHomePagesUseCase {
    suspend fun getHomeBanners(): Flow<DomainNetworkResult<List<Banner>>>
    suspend fun getHomeTopCatalogs(): Flow<DomainNetworkResult<List<TopCatalog>>>
}