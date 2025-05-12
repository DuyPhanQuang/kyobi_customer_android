package com.kyobi.domain.repository

import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.model.TrendingResearch

interface PageRepository {
    suspend fun getBannersFromShopify(
        handle: String,
        key: String
    ): List<Banner>

    suspend fun getTopCatalogsFromShopify(
        handle: String,
        key: String
    ): List<TopCatalog>

    suspend fun getTrendingResearchsFromShopify(
        handle: String,
        key: String
    ): List<TrendingResearch>
}