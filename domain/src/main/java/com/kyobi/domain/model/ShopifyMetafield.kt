package com.kyobi.domain.model

data class ShopifyMetafield(
    val id: String,
    val type: String,
    val key: String,
    val value: String,
    val references: ShopifyReferences? = null,
)