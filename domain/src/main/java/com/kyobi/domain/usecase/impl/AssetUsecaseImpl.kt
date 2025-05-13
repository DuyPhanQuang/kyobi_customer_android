package com.kyobi.domain.usecase.impl

import com.kyobi.domain.model.Assets
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.repository.AppConfigRepository
import com.kyobi.domain.usecase.AssetUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AssetUseCaseImpl @Inject constructor(
    private val appConfigRepository: AppConfigRepository
) : AssetUseCase {
    override suspend fun getGiphyAssets(
        query: String?,
        page: Int?,
        perPage: Int?,
        locale: String?
    ): Flow<DomainNetworkResult<Assets>> {
        return flow {
            val result = appConfigRepository.getAssets(query, page, perPage, locale)
            emit(DomainNetworkResult.Success(result))
        }
    }
}