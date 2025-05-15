package com.kyobi.domain.model

data class ShopifyCollection(
    val id: String? = null,
    val title: String? = null,
    val handle: String? = null,
    val description: String? = null,
    val seo: SEO? = null,
    val updatedAt: String? = null,
    val metafields: List<ShopifyMetafield> = emptyList(),
    val products: List<Product> = emptyList(),
    val pageInfo: PageInfo? = null
)

data class PageInfo(
    val hasNextPage: Boolean,
    val endCursor: String? = null
)