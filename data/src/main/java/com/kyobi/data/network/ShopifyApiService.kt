package com.kyobi.data.network

import com.kyobi.domain.model.Product
import com.kyobi.domain.model.request.MetafieldIdentifierRequest

interface ShopifyApiService {
    suspend fun getProducts(
        query: String? = null,
        reverse: Boolean? = null,
        sortKey: String? = null,
        identifiers: List<MetafieldIdentifierRequest>? = null,
        first: Int? = 250
    ): List<Product>
}