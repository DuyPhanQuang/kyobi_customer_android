package com.kyobi.feature.collection.extension

import com.kyobi.domain.model.SubcategoryMenu
import com.kyobi.feature.collection.screen.collection.model.CollectionMenu
import com.kyobi.feature.collection.screen.collection.model.CollectionMenuType

fun SubcategoryMenu.toCollectionMenu(): CollectionMenu {
    val subCategoryAsCollection = CollectionMenu(
        id = this.id,
        handle = this.handle,
        filterHandle = this.filterHandle,
        title = this.title,
        thumbnail = this.thumbnail,
        thumbnailInfo = this.thumbnailInfo,
        type = CollectionMenuType.SUB_CATEGORY
    )
    return subCategoryAsCollection
}