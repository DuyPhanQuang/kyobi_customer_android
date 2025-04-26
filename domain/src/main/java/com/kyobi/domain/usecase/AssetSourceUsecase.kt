package com.kyobi.domain.usecase

import com.kyobi.domain.model.AssetSource
import com.kyobi.domain.model.DomainNetworkResult
import kotlinx.coroutines.flow.Flow

interface AssetSourceUsecase {
    suspend fun getGiphyAssetSource(): Flow<DomainNetworkResult<AssetSource>>
}