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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.util.lerp
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.composable.R
import com.kyobi.composable.button.AppIconButton
import com.kyobi.theme.kyobiTheme

@Composable
fun HomeSectionHeader(
    topPadding: Dp,
    scrollAlpha: Float,
    onSearchClick: () -> Unit,
    onFavouritesClick: () -> Unit,
    onCartClick: () -> Unit
) {
    val contentColor = Color(
        red = lerp(1f, 0f, scrollAlpha),
        green = lerp(1f, 0f, scrollAlpha),
        blue = lerp(1f, 0f, scrollAlpha),
        alpha = 1f
    )

    val searchBorderColor = Color(
        red = lerp(1f, 0xD6 / 255f, scrollAlpha),
        green = lerp(1f, 0xD3 / 255f, scrollAlpha),
        blue = lerp(1f, 0xD1 / 255f, scrollAlpha),
        alpha = 1f
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.kyobiTheme.height.dp88)
            .background(MaterialTheme.kyobiTheme.colors.onPrimary.copy(alpha = scrollAlpha))
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
            searchBorderColor = searchBorderColor,
            contentColor = contentColor,
            onSearchClick = onSearchClick
        )
        XsSpaceX()
        AppIconButton(
            icon = painterResource(id = R.drawable.ic_favorite),
            onClick = onFavouritesClick,
            iconColor = contentColor
        )
        XsSpaceX()
        AppIconButton(
            icon = painterResource(id = R.drawable.ic_shopping_bag),
            onClick = onCartClick,
            iconColor = contentColor
        )
    }
}