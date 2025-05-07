package com.kyobi.domain.repository

import com.kyobi.domain.model.Product
import com.kyobi.domain.model.request.MetafieldIdentifierRequest

interface ProductRepository {
    suspend fun getProductsFromShopify(
        query: String?,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): List<Product>
}