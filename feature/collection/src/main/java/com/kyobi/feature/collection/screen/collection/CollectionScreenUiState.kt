package com.kyobi.feature.collection.screen.collection

import com.kyobi.feature.collection.screen.collection.model.CollectionMenu

data class CollectionScreenUiState(
    val collectionMenus: List<CollectionMenu>,
    val selectedCollectionId: String? = null,
)