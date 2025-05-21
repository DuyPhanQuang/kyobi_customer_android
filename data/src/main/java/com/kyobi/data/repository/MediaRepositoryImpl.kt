package com.kyobi.data.repository

import com.kyobi.data.network.ShopifyApiService
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.repository.MediaRepository
import javax.inject.Inject

class MediaRepositoryImpl @Inject constructor(
    private val shopifyApiService: ShopifyApiService
): MediaRepository {
    override suspend fun getMediaImagesByIdsFromShopify(
        imageIds: List<String>
    ): List<ShopifyMedia> {
        val response = shopifyApiService.getMediaImagesByIds(
            imageIds = imageIds
        )
        return response
    }

    override suspend fun getDynamicMediasByIdsFromShopify(
        mediaIds: List<String>
    ): List<ShopifyMedia> {
        val response = shopifyApiService.getDynamicMediasByIds(
            mediaIds = mediaIds
        )
        return response
    }

    override suspend fun getMediaImageFromShopify(
        imageId: String
    ): ShopifyMedia? {
        val response = shopifyApiService.getMediaImage(
            imageId = imageId
        )
        return response
    }

}