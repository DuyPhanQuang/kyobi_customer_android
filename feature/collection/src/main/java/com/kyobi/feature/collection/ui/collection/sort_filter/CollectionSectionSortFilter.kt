package com.kyobi.feature.collection.ui.collection.sort_filter

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import com.kyobi.domain.model.CateFilter
import com.kyobi.feature.collection.model.FilterOption
import com.kyobi.feature.collection.ui.collection.sort_filter.content.CollectionColorFilterContent
import com.kyobi.feature.collection.ui.collection.sort_filter.content.CollectionSizeFilterContent
import com.kyobi.feature.collection.ui.collection.sort_filter.content.CollectionSortContent
import com.kyobi.feature.collection.ui.collection.sort_filter.content.mockSorts
import com.kyobi.theme.Colors
import com.kyobi.theme.Dimension
import com.kyobi.theme.kyobiTheme

enum class CollectionSectionSortFilterType { SORT, COLOR_FILTER, SIZE_FILTER }

@Composable
fun CollectionSectionSortFilter(
    cateFilter: CateFilter? = null,
    showDropdown: CollectionSectionSortFilterType? = null,
    updateShowDropdown: (CollectionSectionSortFilterType?) -> Unit,
    onSortClick: () -> Unit,
    selectedFilterOptions: List<FilterOption>,
    onColorFilterClick: () -> Unit,
    toggleColorFilterOption: (FilterOption) -> Unit,
    onColorClearClick: () -> Unit,
    onColorSeeClick: () -> Unit,
    onSizeFilterClick: () -> Unit,
    toggleSizeFilterOption: (FilterOption) -> Unit,
    onSizeClearClick: () -> Unit,
    onSizeSeeClick: () -> Unit,
    onFilterAllClick: () -> Unit,
    viewMode: GridViewModeType,
    onViewModeClick: (GridViewModeType) -> Unit
) {
    val height = MaterialTheme.kyobiTheme.height
    val spacing = MaterialTheme.kyobiTheme.spacing

    Row(
        modifier = Modifier.fillMaxWidth()
            .drawBehind {
                val strokeWidth = Dimension.dp1.toPx()
                val borderColor = Colors().stone100
                drawLine(
                    color = borderColor,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f),
                    strokeWidth = strokeWidth
                )
            }
            .padding(
                vertical = spacing.dp12,
                horizontal = spacing.dp12),
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
                    cateFilter = cateFilter,
                    selectedFilterOptions = selectedFilterOptions,
                    toggleColorFilterOption = toggleColorFilterOption,
                    onClearClick = onColorClearClick,
                    onSeeClick = onColorSeeClick,
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
                    cateFilter = cateFilter,
                    selectedFilterOptions = selectedFilterOptions,
                    toggleSizeFilterOption = toggleSizeFilterOption,
                    onClearClick = onSizeClearClick,
                    onSeeClick = onSizeSeeClick,
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