package com.kyobi.data.repository

import com.kyobi.data.network.ShopifyApiService
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import com.kyobi.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val shopifyApiService: ShopifyApiService
): ProductRepository {
    override suspend fun getProductsFromShopify(
        query: String?,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): List<Product> {
        val response = shopifyApiService.getProducts(
            query,
            reverse,
            sortKey,
            identifiers,
            first
        )
        return response
    }
}