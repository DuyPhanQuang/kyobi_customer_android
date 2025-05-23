package com.kyobi.feature.collection.ui.collection.sort_filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kyobi.domain.model.CateFilter
import com.kyobi.feature.collection.ui.collection.sort_filter.content.CollectionColorFilterContent
import com.kyobi.feature.collection.ui.collection.sort_filter.content.CollectionSizeFilterContent
import com.kyobi.feature.collection.ui.collection.sort_filter.content.CollectionSortContent
import com.kyobi.feature.collection.ui.collection.sort_filter.content.mockSorts
import com.kyobi.theme.kyobiTheme

enum class CollectionSectionSortFilterType { SORT, COLOR_FILTER, SIZE_FILTER }

@Composable
fun CollectionSectionSortFilter(
    modifier: Modifier = Modifier,
    cateFilter: CateFilter? = null,
    showDropdown: CollectionSectionSortFilterType? = null,
    updateShowDropdown: (CollectionSectionSortFilterType?) -> Unit,
    onSortClick: () -> Unit,
    onColorFilterClick: () -> Unit,
    onSizeFilterClick: () -> Unit,
    onFilterAllClick: () -> Unit,
    viewMode: GridViewModeType,
    onViewModeClick: (GridViewModeType) -> Unit
) {
    val height = MaterialTheme.kyobiTheme.height
    val spacing = MaterialTheme.kyobiTheme.spacing

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CustomDropdown(
            height = height.dp120,
            totalSpacing = spacing.dp24 + spacing.dp8,
            focusable = false,
            isActive = showDropdown == CollectionSectionSortFilterType.SORT,
            type = CollectionSectionSortFilterType.SORT,
            currentActive = showDropdown,
            onToggle = { active ->
                if (active && showDropdown != CollectionSectionSortFilterType.SORT) {
                    updateShowDropdown(CollectionSectionSortFilterType.SORT)
                    onSortClick()
                } else if (!active) {
                    updateShowDropdown(null)
                }
            },
            onSwitch = { newType ->
                if (showDropdown != newType) {
                    updateShowDropdown(newType)
                    when (newType) {
                        CollectionSectionSortFilterType.SORT -> onSortClick()
                        CollectionSectionSortFilterType.COLOR_FILTER -> onColorFilterClick()
                        CollectionSectionSortFilterType.SIZE_FILTER -> onSizeFilterClick()
                    }
                }
            },
            popupContent = {
                CollectionSortContent(
                    sortOptions = mockSorts
                )
            },
        ) {
            CollectionSortFilterTile(
                label = "Sort",
                iconDesc = "Sort Icon",
            )
        }
        CustomDropdown(
            height = height.dp244,
            totalSpacing = spacing.dp24 + spacing.dp8,
            focusable = false,
            isActive = showDropdown == CollectionSectionSortFilterType.COLOR_FILTER,
            type = CollectionSectionSortFilterType.COLOR_FILTER,
            currentActive = showDropdown,
            onToggle = { active ->
                if (active && showDropdown != CollectionSectionSortFilterType.COLOR_FILTER) {
                    updateShowDropdown(CollectionSectionSortFilterType.COLOR_FILTER)
                    onColorFilterClick()
                } else if (!active) {
                    updateShowDropdown(null)
                }
            },
            onSwitch = { newType ->
                if (showDropdown != newType) {
                    updateShowDropdown(newType)
                    when (newType) {
                        CollectionSectionSortFilterType.SORT -> onSortClick()
                        CollectionSectionSortFilterType.COLOR_FILTER -> onColorFilterClick()
                        CollectionSectionSortFilterType.SIZE_FILTER -> onSizeFilterClick()
                    }
                }
            },
            popupContent = {
                CollectionColorFilterContent(
                    cateFilter = cateFilter
                )
            },
        ) {
            CollectionSortFilterTile(
                label = "Color",
                iconDesc = "Color Filter Icon",
            )
        }
        CustomDropdown(
            height = height.dp244,
            totalSpacing = spacing.dp24 + spacing.dp8,
            focusable = false,
            isActive = showDropdown == CollectionSectionSortFilterType.SIZE_FILTER,
            type = CollectionSectionSortFilterType.SIZE_FILTER,
            currentActive = showDropdown,
            onToggle = { active ->
                if (active && showDropdown != CollectionSectionSortFilterType.SIZE_FILTER) {
                    updateShowDropdown(CollectionSectionSortFilterType.SIZE_FILTER)
                    onSizeFilterClick()
                } else if (!active) {
                    updateShowDropdown(null)
                }
            },
            onSwitch = { newType ->
                if (showDropdown != newType) {
                    updateShowDropdown(newType)
                    when (newType) {
                        CollectionSectionSortFilterType.SORT -> onSortClick()
                        CollectionSectionSortFilterType.COLOR_FILTER -> onColorFilterClick()
                        CollectionSectionSortFilterType.SIZE_FILTER -> onSizeFilterClick()
                    }
                }
            },
            popupContent = {
                CollectionSizeFilterContent(
                    cateFilter = cateFilter
                )
            },
        ) {
            CollectionSortFilterTile(
                label = "Size",
                iconDesc = "Size Filter Icon",
            )
        }
        CollectionFilterAllTile(
            onItemClick = onFilterAllClick
        )
        GridViewMode(
            mode = viewMode,
            onItemClick = onViewModeClick
        )
    }
}