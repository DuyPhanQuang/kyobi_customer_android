package com.kyobi.feature.collection.ui.collection.sort_filter

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.kyobi.composable.R
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphXs

@Composable
fun CollectionFilterAllTile(
    modifier: Modifier = Modifier,
    isSelected: Boolean = false,
    onItemClick: () -> Unit
) {
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val spacing = MaterialTheme.kyobiTheme.spacing
    val iconTheme = MaterialTheme.kyobiTheme.icon
    val colorTheme = MaterialTheme.kyobiTheme.colors

    val background = if (isSelected) colorTheme.bg.stone100 else Color.Transparent

    Box(
        modifier = modifier
            .clip(MaterialTheme.kyobiTheme.shapes.extraSmall)
            .background(background)
            .clickable { onItemClick() }
    ) {
        Row(
            modifier = Modifier
                .padding(
                    vertical = spacing.dp4,
                    horizontal = spacing.dp8),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier
                    .size(iconTheme.sm),
                painter = painterResource(id = R.drawable.ic_filter),
                contentDescription = "Filter All Icon",
                tint = colorTheme.onBackground,
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