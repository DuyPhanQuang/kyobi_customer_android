package com.kyobi.home.ui.tab

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.home.LookbookItem
import com.kyobi.theme.kyobiTheme

@Composable
fun HomeRecommendedReel(
    modifier: Modifier = Modifier,
    items: List<LookbookItem>,
    imageLoader: ImageLoader
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = MaterialTheme.kyobiTheme.spacing.dp12),
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp8),
        contentPadding = PaddingValues(horizontal = MaterialTheme.kyobiTheme.spacing.dp12),
        content = {
            itemsIndexed(items, key = { _, item -> item.id }) { _, item ->
                ReelItemView(
                    item = item,
                    imageLoader = imageLoader
                )
            }
        }
    )
}

@Composable
fun ReelItemView(
    item: LookbookItem,
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader
) {
    Box(
        modifier = modifier
            .width(MaterialTheme.kyobiTheme.width.dp135)
            .height(MaterialTheme.kyobiTheme.width.dp195)
            .clip(MaterialTheme.kyobiTheme.shapes.medium)
    ) {
        AppImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(MaterialTheme.kyobiTheme.shapes.medium),
            imageUrl = item.imageUrl,
            contentDescription = "Reel item image ${item.id}",
            imageLoader = imageLoader
        )
    }
}
