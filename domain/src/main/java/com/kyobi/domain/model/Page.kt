package com.kyobi.domain.model

data class Page(
    val id: String,
    val title: String,
    val handle: String,
    val body: String,
    val bodySummary: String,
    val seo: SEO? = null,
    val createdAt: String,
    val updatedAt: String,
    val metafield: ShopifyPageMetafield? = null,
)