package com.kyobi.feature.collection.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.kyobi.composable.R
import com.kyobi.composable.button.AppIconButton
import com.kyobi.composable.space.MdSpaceX
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.theme.kyobiTheme

@Composable
fun CollectionCommonSectionHeader(
    modifier: Modifier = Modifier,
    showBackIcon: Boolean = false,
    onBackClick: () -> Unit = {},
    onSearchClick: () -> Unit,
    onFavouritesClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val colorTheme = MaterialTheme.kyobiTheme.colors

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBackIcon) {
            AppIconButton(
                icon = painterResource(id = R.drawable.ic_arrow_left),
                size = MaterialTheme.kyobiTheme.icon.lg,
                onClick = onBackClick,
                iconColor = colorTheme.onBackground,
            )
            MdSpaceX()
        }
        CollectionCommonSearchForm(
            modifier = Modifier.weight(1f),
            onSearchClick = onSearchClick
        )
        XsSpaceX()
        AppIconButton(
            icon = painterResource(id = R.drawable.ic_favorite),
            onClick = onFavouritesClick,
            iconColor = colorTheme.onBackground
        )
        XsSpaceX()
        AppIconButton(
            icon = painterResource(id = R.drawable.ic_shopping_bag),
            onClick = onCartClick,
            iconColor = colorTheme.onBackground
        )
    }
}