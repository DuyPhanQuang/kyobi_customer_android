package com.kyobi.domain.repository

import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.TopCatalog

interface PageRepository {
    suspend fun getBannersFromShopify(
        handle: String,
        key: String
    ): List<Banner>

    suspend fun getTopCatalogsFromShopify(
        handle: String,
        key: String
    ): List<TopCatalog>
}