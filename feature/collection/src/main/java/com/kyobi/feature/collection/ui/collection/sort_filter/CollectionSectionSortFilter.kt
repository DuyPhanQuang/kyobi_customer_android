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

@Composable
fun CollectionSectionSortFilter(
    modifier: Modifier = Modifier,
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
            content = {
                CollectionSortContent(
                    sortOptions = mockSorts
                )
            },
            onClick = onSortClick,
        ) {
            CollectionSortFilterTile(
                label = "Sort",
                iconDesc = "Sort Icon",
            )
        }
        CustomDropdown(
            height = height.dp244,
            totalSpacing = spacing.dp24 + spacing.dp8,
            content = {
                CollectionColorFilterContent(
                    colorFilters = mockColorFilters
                )
            },
            onClick = onColorFilterClick,
        ) {
            CollectionSortFilterTile(
                label = "Color",
                iconDesc = "Color Filter Icon",
            )
        }
        CustomDropdown(
            height = height.dp244,
            totalSpacing = spacing.dp24 + spacing.dp8,
            content = {
                CollectionSizeFilterContent(
                    sizeFilters = mockSizeFilters
                )
            },
            onClick = onSizeFilterClick,
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