package com.kyobi.feature.collection.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import com.kyobi.composable.R
import com.kyobi.composable.space.SmSpaceX
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphXs

@Composable
fun CollectionCommonSearchForm(
    modifier: Modifier,
    onSearchClick: () -> Unit,
) {
    val width = MaterialTheme.kyobiTheme.width
    val height = MaterialTheme.kyobiTheme.height
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val shapeTheme = MaterialTheme.kyobiTheme.shapes
    val iconTheme = MaterialTheme.kyobiTheme.icon
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val spacing = MaterialTheme.kyobiTheme.spacing

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height.dp40)
            .border(
                width.dp1,
                colorTheme.bg.stone300,
                shapeTheme.extraLarge)
            .clip(shapeTheme.extraLarge)
            .background(colorTheme.background)
            .clickable { onSearchClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = spacing.dp8,
                    vertical = spacing.dp8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_normal),
                contentDescription = "Manual search",
                modifier = Modifier.size(iconTheme.lg),
                tint = colorTheme.onBackground
            )
            SmSpaceX()
            Text(
                modifier = Modifier.weight(1f),
                text = "Search",
                color = colorTheme.text.neutral500,
                style = typographyTheme.paragraphXs,
            )
        }
    }
}