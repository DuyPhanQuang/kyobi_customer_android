package com.kyobi.domain.repository

import com.kyobi.domain.model.Banner

interface PageRepository {
    suspend fun getBannersFromShopify(
        handle: String,
        key: String
    ): List<Banner>
}