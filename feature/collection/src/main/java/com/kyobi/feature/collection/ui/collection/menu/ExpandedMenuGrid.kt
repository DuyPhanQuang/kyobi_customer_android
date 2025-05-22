package com.kyobi.feature.collection.ui.collection.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import coil.ImageLoader
import com.kyobi.composable.R
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.feature.collection.screen.collection.model.CollectionMenu
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphXs

@Composable
fun ExpandedMenuGrid(
    modifier: Modifier,
    imageLoader: ImageLoader,
    collectionMenus: List<CollectionMenu>,
    onCollapseClick: () -> Unit,
    selectedCollectionId: String?,
    onItemClick: (CollectionMenu) -> Unit
) {
    val spacing = MaterialTheme.kyobiTheme.spacing
    val width = MaterialTheme.kyobiTheme.width
    val height = MaterialTheme.kyobiTheme.height
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val typographyTheme = MaterialTheme.kyobiTheme.typography

    val tileWidth = width.dp60
    val tileHeight = height.dp68
    val columns = 5
    val rows = 2
    val horizontalSpacing = spacing.dp16
    val contentPaddingHorizontal = spacing.dp12
    val contentPaddingVertical = spacing.dp12
    val gridWidth = tileWidth * columns + horizontalSpacing * columns + tileWidth / 2 // + them `tileWidth / 2` hé 1 phần ui của next category
    val gridHeight = tileHeight * rows + (contentPaddingVertical * rows)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(colorTheme.background)
    ) {
        LazyHorizontalGrid(
            rows = GridCells.Fixed(rows),
            modifier = Modifier
                .width(gridWidth)
                .height(gridHeight),
            horizontalArrangement = Arrangement.spacedBy(horizontalSpacing),
            contentPadding = PaddingValues(horizontal = contentPaddingHorizontal)
        ) {
            itemsIndexed(
                collectionMenus,
                key = { index, _ -> "expanded_menu_$index" }
            ) { _, collectionMenu ->
                CollectionMenuTile(
                    modifier = Modifier
                        .width(tileWidth)
                        .height(tileHeight)
                        .padding(top = contentPaddingVertical),
                    imageLoader = imageLoader,
                    collectionMenu = collectionMenu,
                    isSelected = collectionMenu.id == selectedCollectionId,
                    onItemClick = { onItemClick(collectionMenu) }
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onCollapseClick() },
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = spacing.dp12),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Collapse",
                    style = typographyTheme.paragraphXs,
                    color = colorTheme.primary,
                    textAlign = TextAlign.Center
                )
                XxsSpaceX()
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_up),
                    contentDescription = "Collapse Icon",
                    modifier = Modifier.size(MaterialTheme.kyobiTheme.icon.sm),
                    tint = Color.Unspecified
                )
            }
        }
    }
}