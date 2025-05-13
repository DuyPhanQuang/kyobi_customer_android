package com.kyobi.data.network

import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.ShopifyCollection
import com.kyobi.domain.model.FlashSaleInfo
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.model.TrendingResearch
import com.kyobi.domain.model.request.MetafieldIdentifierRequest

interface ShopifyApiService {
    suspend fun getProducts(
        query: String?,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): List<Product>

    suspend fun getProductRecommendations(
        productId: String,
    ): List<Product>

    suspend fun getProductsByIds(
        ids: List<String>,
        identifiers: List<MetafieldIdentifierRequest>?
    ): List<Product>

    suspend fun getBanners(
        handle: String,
        key: String
    ): List<Banner>

    suspend fun getTopCatalogs(
        handle: String,
        key: String
    ): List<TopCatalog>

    suspend fun getTrendingResearchs(
        handle: String,
        key: String
    ): List<TrendingResearch>

    suspend fun getMediaImagesByIds(
        mediaIds: List<String>
    ): List<ShopifyMedia>

    suspend fun getCollectionProducts(
        handle: String,
        reverse: Boolean?,
        sortKey: String?,
        identifiers: List<MetafieldIdentifierRequest>?,
        first: Int?
    ): ShopifyCollection

    suspend fun getFlashSaleInfosByMetaobjectIds(
        metaobjectIds: List<String>
    ): List<FlashSaleInfo>
}