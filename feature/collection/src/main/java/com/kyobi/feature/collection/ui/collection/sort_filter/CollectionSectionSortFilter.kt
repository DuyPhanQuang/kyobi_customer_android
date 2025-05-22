package com.kyobi.feature.collection.ui.collection.sort_filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val height = MaterialTheme.kyobiTheme.height
    val spacing = MaterialTheme.kyobiTheme.spacing

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CustomDropdown(
            height = height.dp244,
            totalSpacing = spacing.dp24 + spacing.dp8,
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorTheme.bg.red100)
                ) {
                    Text(text = "Featured")
                    Text(text = "Newest Arrivals")
                    Text(text = "Price: High to Low")
                    Text(text = "Price: Low to High")
                }
            },
            onClick = onSortClick,
        ) {
            CollectionSortFilterTile(
                label = "Sort",
                iconDesc = "Sort Icon",
            )
        }
        CollectionSortFilterTile(
            label = "Color",
            iconDesc = "Color Filter Icon",
        )
        CollectionSortFilterTile(
            label = "Size",
            iconDesc = "Size Filter Icon",
        )
        CollectionFilterAllTile(
            onItemClick = onFilterAllClick
        )
        GridViewMode(
            mode = viewMode,
            onItemClick = onViewModeClick
        )
    }
}