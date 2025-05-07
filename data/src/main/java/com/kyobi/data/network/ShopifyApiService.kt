package com.kyobi.data.network

import com.kyobi.domain.model.Product
import com.kyobi.domain.model.request.MetafieldIdentifierRequest

interface ShopifyApiService {
    suspend fun getProducts(
        query: String?,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): List<Product>
}