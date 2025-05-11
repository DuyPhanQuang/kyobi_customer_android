package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.repository.ProductRepository
import com.kyobi.domain.usecase.GetProductRecommendationsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetProductRecommendationsUseCaseImpl @Inject constructor(
    private val productRepository: ProductRepository
) : GetProductRecommendationsUseCase {
    override suspend operator fun invoke(productId: String): Flow<DomainNetworkResult<List<Product>>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val result = productRepository.getProductRecommendationsFromShopify(productId)
                emit(DomainNetworkResult.Success(result))
            } catch (e: ShopifyApiException) {
                emit(DomainNetworkResult.Error.ShopifyApi(e))
            } catch (e: Exception) {
                emit(DomainNetworkResult.Error.Generic(e))
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error.Generic(throwable))
        }
    }
}