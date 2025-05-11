package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import com.kyobi.domain.repository.ProductRepository
import com.kyobi.domain.usecase.GetProductsByIdsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetProductsByIdsUseCaseImpl @Inject constructor(
    private val productRepository: ProductRepository
) : GetProductsByIdsUseCase {
    override suspend operator fun invoke(
        ids: List<String>,
        identifiers: List<MetafieldIdentifierRequest>?
    ): Flow<DomainNetworkResult<List<Product>>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val result = productRepository.getProductsByIdsFromShopify(ids, identifiers)
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