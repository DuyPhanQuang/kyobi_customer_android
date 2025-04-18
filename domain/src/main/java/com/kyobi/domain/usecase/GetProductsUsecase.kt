package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface GetProductsUseCase {
    suspend operator fun invoke(): Flow<DomainNetworkResult<List<Product>>>
}