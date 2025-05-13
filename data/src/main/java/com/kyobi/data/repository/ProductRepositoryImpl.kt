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
            query = query,
            reverse = reverse,
            sortKey = sortKey,
            identifiers = identifiers,
            first = first
        )
        return response
    }

    override suspend fun getProductRecommendationsFromShopify(
        productId: String
    ): List<Product> {
        val response = shopifyApiService.getProductRecommendations(
            productId = productId
        )
        return response
    }

    override suspend fun getProductsByIdsFromShopify(
        ids: List<String>,
        identifiers: List<MetafieldIdentifierRequest>?
    ): List<Product> {
        val response = shopifyApiService.getProductsByIds(
            ids = ids,
            identifiers = identifiers
        )
        return response
    }
}