package com.kyobi.domain.repository

import com.kyobi.domain.model.FlashSaleInfo

interface MetaobjectRepository {
    suspend fun getFlashSaleInfosByMetaobjectIds(
        metaobjectIds: List<String>
    ): List<FlashSaleInfo>
}