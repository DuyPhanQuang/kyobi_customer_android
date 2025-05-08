package com.kyobi.home.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.composable.R
import com.kyobi.composable.button.AppIconButton
import com.kyobi.theme.kyobiTheme

@Composable
fun HomeSectionHeader(
    topPadding: Dp,
    onSearchClick: () -> Unit,
    onFavouritesClick: () -> Unit,
    onCartClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.kyobiTheme.height.dp88)
            .padding(
                start = MaterialTheme.kyobiTheme.spacing.dp12,
                end = MaterialTheme.kyobiTheme.spacing.dp12,
                top = topPadding,
                bottom = MaterialTheme.kyobiTheme.spacing.dp8,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        HomeSearchForm(
            modifier = Modifier.weight(1f),
            onSearchClick = onSearchClick
        )
        XsSpaceX()
        AppIconButton(
            icon = painterResource(id = R.drawable.ic_favorite),
            onClick = onFavouritesClick,
            iconColor = MaterialTheme.kyobiTheme.colors.bg.white
        )
        XsSpaceX()
        AppIconButton(
            icon = painterResource(id = R.drawable.ic_shopping_bag),
            onClick = onCartClick,
            iconColor = MaterialTheme.kyobiTheme.colors.bg.white
        )
    }
}