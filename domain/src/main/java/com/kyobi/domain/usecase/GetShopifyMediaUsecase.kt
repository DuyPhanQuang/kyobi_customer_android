package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.ShopifyMedia
import kotlinx.coroutines.flow.Flow

interface GetShopifyMediaUseCase {
    suspend fun getImagesByIds(imagesIds: List<String>): Flow<DomainNetworkResult<List<ShopifyMedia>>>
}