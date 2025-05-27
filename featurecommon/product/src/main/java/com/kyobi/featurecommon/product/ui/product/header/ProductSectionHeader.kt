package com.kyobi.featurecommon.product.ui.product.header

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import com.kyobi.composable.R
import com.kyobi.composable.button.AppIconButton
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.composable.space.XsSpaceY
import com.kyobi.theme.kyobiTheme

@Composable
fun ProductSectionHeader(
    modifier: Modifier = Modifier,
    onBackClick: () -> Unit,
    onFavouriteClick: () -> Unit,
    onSearchClick: () -> Unit,
    onShareClick: () -> Unit,
) {
    val spacing = MaterialTheme.kyobiTheme.spacing

    Column(
        modifier = modifier
    ) {
        XsSpaceY()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.dp12),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ItemTile(
                icon = painterResource(id = R.drawable.ic_arrow_left),
                onClick = onBackClick,
            )
            Spacer(modifier = Modifier.weight(1f))
            XsSpaceX()
            ItemTile(
                icon = painterResource(id = R.drawable.ic_search_normal),
                onClick = onSearchClick,
            )
            XsSpaceX()
            ItemTile(
                icon = painterResource(id = R.drawable.ic_favorite),
                onClick = onFavouriteClick,
            )
            XsSpaceX()
            ItemTile(
                icon = painterResource(id = R.drawable.ic_share),
                onClick = onShareClick,
            )
        }
    }
}

@Composable
private fun ItemTile(
    onClick: () -> Unit,
    icon: Painter
) {
    val width = MaterialTheme.kyobiTheme.width
    val colorTheme = MaterialTheme.kyobiTheme.colors

    Box(
        modifier = Modifier
            .size(width.dp40)
            .clip(CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .zIndex(0f)
                .matchParentSize()
                .graphicsLayer {
                    renderEffect = BlurEffect(
                        radiusX = 6f,
                        radiusY = 6f,
                        edgeTreatment = TileMode.Clamp)
                }
                .background(colorTheme.onBackground.copy(alpha = 0.2f))
        )
        AppIconButton(
            modifier = Modifier.zIndex(1f),
            icon = icon,
            onClick = onClick,
            iconColor = colorTheme.background,
        )
    }
}