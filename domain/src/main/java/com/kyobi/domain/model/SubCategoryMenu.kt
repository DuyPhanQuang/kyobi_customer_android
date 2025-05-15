package com.kyobi.domain.model

data class SubCategoryGroup(
    val id: String,
    val groupInfo: String,
    val handle: String,
    val type: String,
    val order: Int,
    val label: String,
    val subcategories: List<SubcategoryMenu>? = emptyList()
)

data class SubcategoryMenu(
    val id: String,
    val handle: String,
    val filterHandle: String,
    val title: String,
    val thumbnail: String? = null,
    val thumbnailInfo: ShopifyMedia? = null
)
