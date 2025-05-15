package com.kyobi.domain.model

data class ShopifyMetaobject(
    val id: String,
    val handle: String,
    val type: String? = null,
    val fields: List<ShopifyMetaobjectField>? = emptyList()
)

data class ShopifyMetaobjectField(
    val key: String,
    val value: String? = null
)