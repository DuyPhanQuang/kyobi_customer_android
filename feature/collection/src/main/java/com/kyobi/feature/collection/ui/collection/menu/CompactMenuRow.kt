package com.kyobi.feature.collection.ui.collection.menu

import android.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import coil.ImageLoader
import com.kyobi.composable.R
import com.kyobi.feature.collection.screen.collection.model.CollectionMenu
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallXs

@Composable
fun CompactMenuRow(
    modifier: Modifier,
    imageLoader: ImageLoader,
    collectionMenus: List<CollectionMenu>,
    onAllClick: () -> Unit,
    selectedCollectionId: String?,
    onItemClick: (CollectionMenu) -> Unit
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val iconSize = MaterialTheme.kyobiTheme.icon.lg
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val allContainerWidth = spacing.dp48

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(MaterialTheme.kyobiTheme.height.dp84)
            .background(colorTheme.background),
        contentAlignment = Alignment.Center
    ) {
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(spacing.dp16),
            contentPadding = PaddingValues(
                start = spacing.dp12,
                end = spacing.dp12 + allContainerWidth
            )
        ) {
            items(
                collectionMenus,
                key = { "compact_menu_${it.id}_${it.filterHandle}" }
            ) { collectionMenu ->
                CollectionMenuTile(
                    modifier = Modifier.padding(
                        top = spacing.dp8
                    ),
                    imageLoader = imageLoader,
                    collectionMenu = collectionMenu,
                    isSelected = collectionMenu.id == selectedCollectionId,
                    onItemClick = { onItemClick(collectionMenu) }
                )
            }
        }

        Box(
            modifier = Modifier
                .width(allContainerWidth)
                .fillMaxHeight()
                .padding(vertical = spacing.dp1)
                .align(Alignment.TopEnd)
                .clickable { onAllClick() }
                .drawBehind {
                    val paint = Paint().asFrameworkPaint().apply {
                        isAntiAlias = true
                        setShadowLayer(4f, -2f, 0f, Color.argb(25, 0, 0, 0))
                    }
                    drawContext.canvas.nativeCanvas.apply {
                        save()
                        drawRect(0f, 0f, size.width, size.height, paint)
                        restore()
                    }
                }
                .graphicsLayer {
                    shadowElevation = 0f
                    shape = RectangleShape
                    clip = false
                }
                .background(colorTheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.CenterHorizontally
            ){
                Icon(
                    modifier = Modifier
                        .size(iconSize)
                        .padding(top = spacing.dp8),
                    painter = painterResource(id = R.drawable.ic_category),
                    contentDescription = "All Category Icon",
                )
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.dp12),
                    text = "All",
                    style = typographyTheme.labelSmallXs,
                    color = colorTheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}