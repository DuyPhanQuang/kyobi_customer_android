package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import kotlinx.coroutines.flow.Flow

interface GetProductsByIdsUseCase {
    suspend operator fun invoke(
        ids: List<String>,
        identifiers: List<MetafieldIdentifierRequest>?
    ): Flow<DomainNetworkResult<List<Product>>>
}