package com.kyobi.domain.model

data class ShopifyPageMetafield(
    val id: String,
    val references: ShopifyReferences
)

data class ShopifyReferences(
    val nodes: List<ShopifyMetaobject>
)