package com.kyobi.domain.usecase

import com.kyobi.domain.model.AssetSource
import com.kyobi.domain.model.DomainNetworkResult
import kotlinx.coroutines.flow.Flow

interface AssetSourceUseCase {
    suspend fun getGiphyAssetSource(): Flow<DomainNetworkResult<AssetSource>>
}