package com.kyobi.home.ui.tab

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.kyobi.composable.R
import com.kyobi.composable.space.SmSpaceX
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphXs

@Composable
fun HomeSearchForm(
    modifier: Modifier,
    onSearchClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MaterialTheme.kyobiTheme.height.dp40)
            .clip(MaterialTheme.kyobiTheme.shapes.extraLarge)
            .background(MaterialTheme.kyobiTheme.colors.background)
            .border(
                MaterialTheme.kyobiTheme.width.dp1,
                Color.Transparent,
                MaterialTheme.kyobiTheme.shapes.extraLarge)
            .clickable { onSearchClick() },
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = MaterialTheme.kyobiTheme.spacing.dp8,
                vertical = MaterialTheme.kyobiTheme.spacing.dp8
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_search_normal),
                contentDescription = "Manual search",
                modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.lg),
                tint = MaterialTheme.kyobiTheme.colors.bg.stone500
            )
            SmSpaceX()
            Text(
                modifier = Modifier.weight(1f),
                text = "Search",
                color = MaterialTheme.kyobiTheme.colors.text.neutral500,
                style = MaterialTheme.kyobiTheme.typography.paragraphXs,
            )
        }
    }
}