package com.kyobi.domain.usecase.impl

import com.kyobi.domain.model.AssetSource
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.repository.AppConfigRepository
import com.kyobi.domain.usecase.AssetSourceUsecase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AssetSourceUsecaseImpl @Inject constructor(
    private val appConfigRepository: AppConfigRepository
) : AssetSourceUsecase {
    override suspend fun getGiphyAssetSource(): Flow<DomainNetworkResult<AssetSource>> {
        return flow {
            val result = appConfigRepository.getAssetSource()
            emit(DomainNetworkResult.Success(result))
        }
    }
}