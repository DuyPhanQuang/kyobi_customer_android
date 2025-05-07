package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import kotlinx.coroutines.flow.Flow

interface GetProductsUseCase {
    suspend operator fun invoke(
        query: String?,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): Flow<DomainNetworkResult<List<Product>>>
}