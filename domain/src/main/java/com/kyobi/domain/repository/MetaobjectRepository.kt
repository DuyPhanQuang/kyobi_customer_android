package com.kyobi.domain.repository

import com.kyobi.domain.model.CateFilter
import com.kyobi.domain.model.FlashSaleInfo
import com.kyobi.domain.model.ShopifyMetaobject

interface MetaobjectRepository {
    suspend fun getFlashSaleInfosByMetaobjectIds(
        metaobjectIds: List<String>
    ): List<FlashSaleInfo>

    suspend fun getMetaobjectsByIds(
        metaobjectIds: List<String>
    ): List<ShopifyMetaobject>

    suspend fun getFilterSetByCateHandle(
        handle: String
    ): CateFilter?
}