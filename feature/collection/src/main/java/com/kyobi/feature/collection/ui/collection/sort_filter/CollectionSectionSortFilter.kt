package com.kyobi.feature.collection.ui.collection.sort_filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
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
    onFilterAllClick: () -> Unit
) {
    val colorTheme = MaterialTheme.kyobiTheme.colors

    Row(
        modifier = modifier
            .background(colorTheme.background),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        CollectionSortFilterTile(
            label = "Sort",
            iconDesc = "Sort Icon",
            onItemClick = onSortClick
        )
        CollectionSortFilterTile(
            label = "Color",
            iconDesc = "Color Filter Icon",
            onItemClick = onColorFilterClick
        )
        CollectionSortFilterTile(
            label = "Size",
            iconDesc = "Size Filter Icon",
            onItemClick = onSizeFilterClick
        )
        CollectionFilterAllTile(
            onItemClick = onFilterAllClick
        )
    }
}