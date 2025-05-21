package com.kyobi.domain.model

data class CategoryMenu(
    val id: String,
    val handle: String,
    val filterHandle: String,
    val groups: List<SubCategoryGroup>? = emptyList(),
    val title: String,
    val order: Int,
    val thumbnail: String? = null,
    val thumbnailInfo: ShopifyMedia? = null
)