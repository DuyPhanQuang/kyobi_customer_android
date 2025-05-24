package com.kyobi.home.ui.tab.reels

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
    val spacing = MaterialTheme.kyobiTheme.spacing

    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = spacing.dp12),
        horizontalArrangement = Arrangement.spacedBy(spacing.dp8),
        contentPadding = PaddingValues(horizontal = spacing.dp12),
        content = {
            itemsIndexed(
                items,
                key = { index, _ -> "recommended_reel_$index" }
            ) { _, item ->
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
    val width = MaterialTheme.kyobiTheme.width
    val height = MaterialTheme.kyobiTheme.height
    val shapeTheme = MaterialTheme.kyobiTheme.shapes

    Box(
        modifier = modifier
            .width(width.dp135)
            .height(height.dp195)
            .clip(shapeTheme.medium)
    ) {
        AppImage(
            modifier = Modifier
                .fillMaxSize()
                .clip(shapeTheme.medium),
            imageUrl = item.imageUrl,
            contentDescription = "Recommended reel image ${item.id}",
            imageLoader = imageLoader
        )
    }
}
