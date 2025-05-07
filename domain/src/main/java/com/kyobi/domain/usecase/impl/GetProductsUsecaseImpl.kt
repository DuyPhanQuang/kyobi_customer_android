package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import com.kyobi.domain.repository.ProductRepository
import com.kyobi.domain.usecase.GetProductsUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import javax.inject.Inject

class GetProductsUseCaseImpl @Inject constructor(
    private val productRepository: ProductRepository
): GetProductsUseCase {
    override suspend operator fun invoke(
        query: String?,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): Flow<DomainNetworkResult<List<Product>>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val result = productRepository.getProductsFromShopify(
                    query = query,
                    reverse = reverse,
                    sortKey = sortKey,
                    identifiers = identifiers,
                    first = first
                )
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