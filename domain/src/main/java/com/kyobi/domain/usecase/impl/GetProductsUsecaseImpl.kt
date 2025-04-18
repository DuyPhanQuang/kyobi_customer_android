package com.kyobi.domain.usecase.impl

import com.kyobi.core.model.NetworkResult
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.repository.ProductRepository
import com.kyobi.domain.usecase.GetProductsUseCase
import javax.inject.Inject

class GetProductsUseCaseImpl @Inject constructor(
    private val productRepository: ProductRepository
): GetProductsUseCase {
    override suspend operator fun invoke(): DomainNetworkResult<List<Product>> {
        return when (val result = productRepository.getProductsFromShopify()) {
            is NetworkResult.Success -> {
                DomainNetworkResult.Success(result.data)
            }
            is NetworkResult.Error -> {
                DomainNetworkResult.Error(result.exception)
            }
            is NetworkResult.Loading -> {
                DomainNetworkResult.Loading
            }
        }
    }
}