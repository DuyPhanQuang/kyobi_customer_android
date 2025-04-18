package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product

interface GetProductsUseCase {
    suspend operator fun invoke(): DomainNetworkResult<List<Product>>
}