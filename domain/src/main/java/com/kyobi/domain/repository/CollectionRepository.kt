package com.kyobi.domain.repository

import com.kyobi.domain.model.ShopifyCollection
import com.kyobi.domain.model.request.MetafieldIdentifierRequest

interface CollectionRepository {
    suspend fun getCollectionProducts(
        handle: String,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): ShopifyCollection

    suspend fun getCollectionsLarge(
        handle: String,
        identifiers: List<MetafieldIdentifierRequest>?
    ): ShopifyCollection
}