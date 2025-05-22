package com.kyobi.feature.collection.ui.collection.sort_filter

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.kyobi.composable.R
import com.kyobi.composable.space.MdSpaceX
import com.kyobi.theme.kyobiTheme

enum class GridViewModeType { COLUMNS_2, COLUMNS_3 }

@Composable
fun GridViewMode(
    modifier: Modifier = Modifier,
    mode: GridViewModeType,
    onItemClick: (GridViewModeType) -> Unit
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val colorTheme = MaterialTheme.kyobiTheme.colors

    fun isModeSelected(current: GridViewModeType): Boolean = mode == current

    Row(
        modifier = modifier
            .padding(
                vertical = spacing.dp4,
                horizontal = spacing.dp8),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier
                .clickable { onItemClick(GridViewModeType.COLUMNS_2) },
            painter = painterResource(id = R.drawable.ic_two_colums),
            contentDescription = "Columns 2 Icon",
            tint = if (isModeSelected(GridViewModeType.COLUMNS_2))
                colorTheme.bg.neutral950 else colorTheme.bg.stone200,
        )
        MdSpaceX()
        Icon(
            modifier = Modifier
                .clickable { onItemClick(GridViewModeType.COLUMNS_3) },
            painter = painterResource(id = R.drawable.ic_three_colums),
            contentDescription = "Columns 3 Icon",
            tint = if (isModeSelected(GridViewModeType.COLUMNS_3))
                colorTheme.bg.neutral950 else colorTheme.bg.stone200,
        )
    }
}