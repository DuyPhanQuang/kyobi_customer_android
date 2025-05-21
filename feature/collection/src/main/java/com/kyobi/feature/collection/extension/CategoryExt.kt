package com.kyobi.feature.collection.extension

import com.kyobi.domain.model.CategoryMenu
import com.kyobi.feature.collection.screen.collection.model.CollectionMenu
import com.kyobi.feature.collection.screen.collection.model.CollectionMenuType

fun CategoryMenu.toCollectionMenus(): List<CollectionMenu> {
    val categoryAsCollection = CollectionMenu(
        id = this.id,
        handle = this.handle,
        filterHandle = this.filterHandle,
        title = this.title,
        thumbnail = this.thumbnail,
        thumbnailInfo = this.thumbnailInfo,
        type = CollectionMenuType.CATEGORY
    )
    val subcategoryMenus = this.groups
        ?.flatMap { it.subcategories ?: emptyList() }
        ?: emptyList()
    val subcategoryCollections = subcategoryMenus.map { sub ->
        CollectionMenu(
            id = sub.id,
            handle = sub.handle,
            filterHandle = sub.filterHandle,
            title = sub.title,
            thumbnail = sub.thumbnail,
            thumbnailInfo = sub.thumbnailInfo,
            type = CollectionMenuType.SUB_CATEGORY
        )
    }
    return listOf(categoryAsCollection) + subcategoryCollections
}