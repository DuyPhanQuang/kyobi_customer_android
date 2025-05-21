package com.kyobi.domain.repository

import com.kyobi.domain.model.ShopifyMedia

interface MediaRepository {
    suspend fun getMediaImagesByIdsFromShopify(
        imageIds: List<String>
    ): List<ShopifyMedia>

    suspend fun getDynamicMediasByIdsFromShopify(
        mediaIds: List<String>
    ): List<ShopifyMedia>

    suspend fun getMediaImageFromShopify(
        imageId: String
    ): ShopifyMedia?
}