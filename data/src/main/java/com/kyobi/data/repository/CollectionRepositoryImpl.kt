package com.kyobi.data.repository

import com.kyobi.data.network.ShopifyApiService
import com.kyobi.domain.model.ShopifyCollection
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import com.kyobi.domain.repository.CollectionRepository
import javax.inject.Inject

class CollectionRepositoryImpl @Inject constructor(
    private val shopifyApiService: ShopifyApiService,
): CollectionRepository {
    override suspend fun getCollectionProducts(
        handle: String,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): ShopifyCollection {
        val response = shopifyApiService.getCollectionProducts(
            handle = handle,
            reverse = reverse,
            sortKey = sortKey,
            identifiers = identifiers,
            first = first
        )
        return response
    }

}