package com.kyobi.domain.model

data class ShopifyCollection(
    val id: String,
    val title: String,
    val metafields: List<ShopifyMetafield> = emptyList(),
    val products: List<Product>,
    val pageInfo: PageInfo? = null
)

data class PageInfo(
    val hasNextPage: Boolean,
    val endCursor: String? = null
)