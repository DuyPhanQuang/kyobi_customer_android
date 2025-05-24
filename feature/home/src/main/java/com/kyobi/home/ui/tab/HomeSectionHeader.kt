package com.kyobi.home.ui.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.composable.R
import com.kyobi.composable.button.AppIconButton
import com.kyobi.theme.kyobiTheme

@Composable
fun HomeSectionHeader(
    modifier: Modifier,
    onSearchClick: () -> Unit,
    onFavouritesClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val colorTheme = MaterialTheme.kyobiTheme.colors

    Row(
        modifier = modifier,
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
            iconColor = colorTheme.bg.white
        )
        XsSpaceX()
        AppIconButton(
            icon = painterResource(id = R.drawable.ic_shopping_bag),
            onClick = onCartClick,
            iconColor = colorTheme.bg.white
        )
    }
}