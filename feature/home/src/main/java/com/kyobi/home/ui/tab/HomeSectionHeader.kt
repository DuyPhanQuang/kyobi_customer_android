package com.kyobi.home.ui.tab

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val iconColor = Color(
        red = lerp(1f, 0f, scrollAlpha),
        green = lerp(1f, 0f, scrollAlpha),
        blue = lerp(1f, 0f, scrollAlpha),
        alpha = 1f
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.kyobiTheme.height.dp88)
            .background(MaterialTheme.kyobiTheme.colors.background.copy(alpha = scrollAlpha))
            .padding(
                start = MaterialTheme.kyobiTheme.spacing.dp12,
                end = MaterialTheme.kyobiTheme.spacing.dp12,
                top = topPadding,
                bottom = MaterialTheme.kyobiTheme.spacing.dp8,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.kyobiTheme.height.dp40)
                .weight(1f)
                .clip(MaterialTheme.kyobiTheme.shapes.extraLarge)
                .background(Color.Transparent)
                .clickable { onSearchClick() },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = MaterialTheme.kyobiTheme.spacing.dp12
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search_normal),
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp),
                    tint = Color.Gray
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Search products...",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
        XsSpaceX()
        AppIconButton(
            icon = painterResource(id = R.drawable.ic_favorite),
            onClick = onFavouritesClick,
            iconColor = iconColor
        )
        XsSpaceX()
        AppIconButton(
            icon = painterResource(id = R.drawable.ic_shopping_bag),
            onClick = onCartClick,
            iconColor = iconColor
        )
    }
}