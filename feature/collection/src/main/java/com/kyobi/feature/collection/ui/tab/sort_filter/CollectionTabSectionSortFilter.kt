package com.kyobi.feature.collection.ui.tab.sort_filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kyobi.theme.kyobiTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.res.painterResource
import com.kyobi.composable.R
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.theme.Colors
import com.kyobi.theme.Dimension
import com.kyobi.theme.paragraphXs

@Composable
fun CollectionTabSectionSortFilter(
    modifier: Modifier = Modifier,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val spacing = MaterialTheme.kyobiTheme.spacing
    val iconTheme = MaterialTheme.kyobiTheme.icon
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val shapesTheme = MaterialTheme.kyobiTheme.shapes

    Row(
        modifier = modifier
            .background(colorTheme.background)
            .drawBehind {
                val strokeWidth = Dimension.dp1.toPx()
                val borderColor = Colors().stone100
                drawLine(
                    color = borderColor,
                    start = Offset(0f, size.height),
                    end = Offset(size.width, size.height),
                    strokeWidth = strokeWidth
                )
            },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .clip(shapesTheme.extraSmall)
                .clickable { onSortClick() }
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        vertical = spacing.dp12,
                        horizontal = spacing.dp8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Sort",
                    style = typographyTheme.paragraphXs,
                    color = colorTheme.onBackground,
                )
                XxsSpaceX()
                Icon(
                    modifier = Modifier
                        .size(iconTheme.sm),
                    painter = painterResource(id = R.drawable.ic_arrow_down),
                    contentDescription = "Sort Icon",
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(shapesTheme.extraSmall)
                .clickable { onFilterClick() }
        ) {
            Row(
                modifier = Modifier
                    .padding(
                        vertical = spacing.dp12,
                        horizontal = spacing.dp8),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    modifier = Modifier.size(iconTheme.sm),
                    painter = painterResource(id = R.drawable.ic_filter),
                    contentDescription = "Filter Icon",
                )
                XxsSpaceX()
                Text(
                    text = "Filter",
                    style = typographyTheme.paragraphXs,
                    color = colorTheme.onBackground,
                )
            }
        }
    }
}