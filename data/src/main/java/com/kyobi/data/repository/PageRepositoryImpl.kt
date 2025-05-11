package com.kyobi.data.repository

import com.kyobi.data.network.ShopifyApiService
import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.repository.PageRepository
import javax.inject.Inject

class PageRepositoryImpl @Inject constructor(
    private val shopifyApiService: ShopifyApiService
) : PageRepository {
    override suspend fun getBannersFromShopify(
        handle: String,
        key: String
    ): List<Banner> {
        return shopifyApiService.getBanners(
            handle = handle,
            key = key
        )
    }

    override suspend fun getTopCatalogsFromShopify(
        handle: String,
        key: String
    ): List<TopCatalog> {
        return shopifyApiService.getTopCatalogs(
            handle = handle,
            key = key
        )
    }
}