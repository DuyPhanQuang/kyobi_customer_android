package com.kyobi.feature.collection.ui.collection.sort_filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kyobi.feature.collection.ui.collection.sort_filter.content.CollectionColorFilterContent
import com.kyobi.feature.collection.ui.collection.sort_filter.content.CollectionSizeFilterContent
import com.kyobi.feature.collection.ui.collection.sort_filter.content.CollectionSortContent
import com.kyobi.feature.collection.ui.collection.sort_filter.content.mockColorFilters
import com.kyobi.feature.collection.ui.collection.sort_filter.content.mockSizeFilters
import com.kyobi.feature.collection.ui.collection.sort_filter.content.mockSorts
import com.kyobi.theme.kyobiTheme

enum class CollectionSectionSortFilterType { SORT, COLOR_FILTER, SIZE_FILTER }

@Composable
fun CollectionSectionSortFilter(
    modifier: Modifier = Modifier,
    activeDropdown: CollectionSectionSortFilterType? = null,
    updateActiveDropdown: (CollectionSectionSortFilterType?) -> Unit,
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
            isActive = activeDropdown == CollectionSectionSortFilterType.SORT,
            type = CollectionSectionSortFilterType.SORT,
            currentActive = activeDropdown,
            onToggle = { active ->
                if (active && activeDropdown != CollectionSectionSortFilterType.SORT) {
                    updateActiveDropdown(CollectionSectionSortFilterType.SORT)
                    onSortClick()
                } else if (!active) {
                    updateActiveDropdown(null)
                }
            },
            onSwitch = { newType ->
                if (activeDropdown != newType) {
                    updateActiveDropdown(newType)
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
            isActive = activeDropdown == CollectionSectionSortFilterType.COLOR_FILTER,
            type = CollectionSectionSortFilterType.COLOR_FILTER,
            currentActive = activeDropdown,
            onToggle = { active ->
                if (active && activeDropdown != CollectionSectionSortFilterType.COLOR_FILTER) {
                    updateActiveDropdown(CollectionSectionSortFilterType.COLOR_FILTER)
                    onColorFilterClick()
                } else if (!active) {
                    updateActiveDropdown(null)
                }
            },
            onSwitch = { newType ->
                if (activeDropdown != newType) {
                    updateActiveDropdown(newType)
                    when (newType) {
                        CollectionSectionSortFilterType.SORT -> onSortClick()
                        CollectionSectionSortFilterType.COLOR_FILTER -> onColorFilterClick()
                        CollectionSectionSortFilterType.SIZE_FILTER -> onSizeFilterClick()
                    }
                }
            },
            popupContent = {
                CollectionColorFilterContent(
                    colorFilters = mockColorFilters
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
            isActive = activeDropdown == CollectionSectionSortFilterType.SIZE_FILTER,
            type = CollectionSectionSortFilterType.SIZE_FILTER,
            currentActive = activeDropdown,
            onToggle = { active ->
                if (active && activeDropdown != CollectionSectionSortFilterType.SIZE_FILTER) {
                    updateActiveDropdown(CollectionSectionSortFilterType.SIZE_FILTER)
                    onSizeFilterClick()
                } else if (!active) {
                    updateActiveDropdown(null)
                }
            },
            onSwitch = { newType ->
                if (activeDropdown != newType) {
                    updateActiveDropdown(newType)
                    when (newType) {
                        CollectionSectionSortFilterType.SORT -> onSortClick()
                        CollectionSectionSortFilterType.COLOR_FILTER -> onColorFilterClick()
                        CollectionSectionSortFilterType.SIZE_FILTER -> onSizeFilterClick()
                    }
                }
            },
            popupContent = {
                CollectionSizeFilterContent(
                    sizeFilters = mockSizeFilters
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