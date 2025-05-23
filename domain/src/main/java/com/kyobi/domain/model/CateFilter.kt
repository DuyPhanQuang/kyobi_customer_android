package com.kyobi.domain.model

data class ShopifyCateFilter(
    val id: String,
    val handle: String,
    val fields: List<ShopifyCateMetaobjectField>
)

data class ShopifyCateMetaobjectField(
    val key: String,
    val value: String? = null,
    val type: String,
    val references: ShopifyReferences
)

data class CateFilter(
    val filterSetId: String,
    val cateHandle: String,
    val fields: List<CateFilterMetaobjectField>
)

data class CateFilterMetaobjectField(
    val label: String,
    val key: String, // use with namespace for filter in FE
    val originalKey: String,
    val ids: List<String>,
    val selectedIds: List<String>,
    val references: ShopifyReferences
)