package com.kyobi.domain.usecase.impl

import com.kyobi.core.model.NetworkResult
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.repository.ProductRepository
import com.kyobi.domain.usecase.GetProductsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetProductsUseCaseImpl @Inject constructor(
    private val productRepository: ProductRepository
): GetProductsUseCase {
    override suspend operator fun invoke(): Flow<DomainNetworkResult<List<Product>>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            when (val result = productRepository.getProductsFromShopify()) {
                is NetworkResult.Success -> emit(DomainNetworkResult.Success(result.data))
                is NetworkResult.Error -> emit(DomainNetworkResult.Error(result.exception))
                is NetworkResult.Loading -> emit(DomainNetworkResult.Loading)
            }
        }
    }
}