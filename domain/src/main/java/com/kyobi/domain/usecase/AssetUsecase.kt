package com.kyobi.domain.usecase

import com.kyobi.domain.model.Assets
import com.kyobi.domain.model.DomainNetworkResult
import kotlinx.coroutines.flow.Flow

interface AssetUsecase {
    suspend fun getGiphyAssets(
        query: String?,
        page: Int?,
        perPage: Int?,
        locale: String?
    ): Flow<DomainNetworkResult<Assets>>
}