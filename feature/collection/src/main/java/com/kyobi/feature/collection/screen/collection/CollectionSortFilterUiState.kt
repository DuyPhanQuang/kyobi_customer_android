package com.kyobi.feature.collection.screen.collection

import com.kyobi.feature.collection.model.FilterOption
import com.kyobi.feature.collection.ui.collection.sort_filter.content.SortOption

data class CollectionSortFilterUiState(
    val selectedFilterOptions: List<FilterOption>,
    val selectedSortOption: SortOption? = null
)