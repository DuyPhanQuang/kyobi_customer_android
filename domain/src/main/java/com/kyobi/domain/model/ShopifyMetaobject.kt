package com.kyobi.domain.model

data class ShopifyMetaobject(
    val id: String,
    val handle: String,
    val type: String,
    val fields: List<ShopifyMetaobjectField>
)

data class ShopifyMetaobjectField(
    val key: String,
    val value: String
)