package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.FlashSale
import com.kyobi.domain.repository.CollectionRepository
import com.kyobi.domain.repository.MetaobjectRepository
import com.kyobi.domain.repository.ProductRepository
import com.kyobi.domain.usecase.GetFlashSaleUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.catch
import javax.inject.Inject

class GetFlashSaleUseCaseImpl @Inject constructor(
    private val collectionRepository: CollectionRepository,
    private val metaobjectRepository: MetaobjectRepository,
    private val productRepository: ProductRepository
) : GetFlashSaleUseCase {
    override suspend fun getFlashSale(handle: String): Flow<DomainNetworkResult<FlashSale>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                // Step1: get metaobject id từ collection
                val collection = collectionRepository.getCollectionProducts(
                    handle = handle,
                    reverse = null,
                    sortKey = null,
                    identifiers = null,
                    first = null)
                val metaobjectId = collection.metafields.find { metafield ->
                    metafield.key == "flash-sale-disco"
                }?.value ?: throw ShopifyApiException(
                    message = "Flashsale metaobject ID not found in collection metafields",
                    errorCode = null)
                // Step2: get FlashSaleInfo từ metaobject id
                val flashSaleInfos = metaobjectRepository.getFlashSaleInfosByMetaobjectIds(
                    metaobjectIds = listOf(metaobjectId))
                val flashSaleInfo = flashSaleInfos.firstOrNull() ?: throw ShopifyApiException(
                    message = "Flashsale info not found for metaobject ID: $metaobjectId",
                    errorCode = null)
                // Step3: get list product từ productIds
                val products = if (flashSaleInfo.productIds.isNotEmpty()) {
                    productRepository.getProductsByIdsFromShopify(
                        ids = flashSaleInfo.productIds,
                        identifiers = null)
                } else {
                    emptyList()
                }
                val flashSale = FlashSale(
                    flashSaleInfo = flashSaleInfo,
                    products = products
                )
                emit(DomainNetworkResult.Success(flashSale))
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