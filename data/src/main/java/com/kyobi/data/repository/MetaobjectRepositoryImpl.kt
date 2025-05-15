package com.kyobi.data.repository

import com.kyobi.data.network.ShopifyApiService
import com.kyobi.domain.model.FlashSaleInfo
import com.kyobi.domain.model.ShopifyMetaobject
import com.kyobi.domain.repository.MetaobjectRepository
import javax.inject.Inject

class MetaobjectRepositoryImpl @Inject constructor(
    private val shopifyApiService: ShopifyApiService
): MetaobjectRepository {
    override suspend fun getFlashSaleInfosByMetaobjectIds(
        metaobjectIds: List<String>
    ): List<FlashSaleInfo> {
        val response = shopifyApiService.getFlashSaleInfosByMetaobjectIds(
            metaobjectIds = metaobjectIds
        )
        return response
    }

    override suspend fun getMetaobjectsByIds(
        metaobjectIds: List<String>
    ): List<ShopifyMetaobject> {
        val response = shopifyApiService.getMetaobjectsByIds(
            metaobjectIds = metaobjectIds
        )
        return response
    }
}