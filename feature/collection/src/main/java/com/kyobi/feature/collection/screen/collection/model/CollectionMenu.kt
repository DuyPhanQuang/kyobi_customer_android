package com.kyobi.feature.collection.screen.collection.model

import com.kyobi.domain.model.ShopifyMedia

enum class CollectionMenuType {
    CATEGORY,
    SUB_CATEGORY
}

data class CollectionMenu(
    val id: String,
    val handle: String,
    val filterHandle: String,
    val title: String,
    val thumbnail: String? = null,
    val thumbnailInfo: ShopifyMedia? = null,
    val type: CollectionMenuType
)