package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface GetProductRecommendationsUseCase {
    suspend operator fun invoke(productId: String): Flow<DomainNetworkResult<List<Product>>>
}