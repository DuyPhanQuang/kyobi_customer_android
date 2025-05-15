package com.kyobi.feature.collection.ui.tab.sort_filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.res.painterResource
import com.kyobi.composable.R
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.theme.paragraphXs

@Composable
fun CollectionSectionSortFilter(
    modifier: Modifier = Modifier,
) {
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val spacing = MaterialTheme.kyobiTheme.spacing
    val iconTheme = MaterialTheme.kyobiTheme.icon

    Row(
        modifier = modifier
            .background(MaterialTheme.kyobiTheme.colors.background)
            .padding(
            vertical = spacing.dp8,
            horizontal = spacing.dp12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier
                .padding(
                vertical = spacing.dp4,
                horizontal = spacing.dp8
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Sort",
                style = typographyTheme.paragraphXs,
                color = MaterialTheme.kyobiTheme.colors.onBackground,
            )
            XxsSpaceX()
            Icon(
                modifier = Modifier
                    .size(iconTheme.sm),
                painter = painterResource(id = R.drawable.ic_arrow_down),
                contentDescription = "Sort Icon",
            )
        }
        Row(
            modifier = Modifier.padding(
                vertical = spacing.dp4,
                horizontal = spacing.dp8
            ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(iconTheme.sm),
                painter = painterResource(id = R.drawable.ic_filter),
                contentDescription = "Filter Icon",
            )
            XxsSpaceX()
            Text(
                text = "Filter",
                style = typographyTheme.paragraphXs,
                color = MaterialTheme.kyobiTheme.colors.onBackground,
            )
        }
    }
}