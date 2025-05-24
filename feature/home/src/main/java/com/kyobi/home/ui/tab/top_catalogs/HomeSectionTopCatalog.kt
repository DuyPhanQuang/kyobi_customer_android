package com.kyobi.home.ui.tab.top_catalogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.domain.model.TopCatalog
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallXs

@Composable
fun HomeSectionTopCatalog(
    items: List<TopCatalog>,
    imageLoader: ImageLoader,
) {
    val itemsPerRow = 5
    val rows = items.chunked(itemsPerRow)

    val spacing = MaterialTheme.kyobiTheme.spacing

    Column(
        modifier = Modifier
            .padding(bottom = spacing.dp8)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.dp8),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp12)
            ) {
                rowItems.forEach { catalog ->
                    TopCatalogTile(
                        modifier = Modifier.weight(1f),
                        catalog = catalog,
                        imageLoader = imageLoader,
                        onItemClick = {  },
                    )
                }
                repeat(itemsPerRow - rowItems.size) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

@Composable
fun TopCatalogTile(
    modifier: Modifier = Modifier,
    catalog: TopCatalog,
    imageLoader: ImageLoader,
    onItemClick: () -> Unit,
) {
    val imageData = catalog.image?.image

    val spacing = MaterialTheme.kyobiTheme.spacing
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val width = MaterialTheme.kyobiTheme.width

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .padding(top = spacing.dp8)
            .clickable { onItemClick() }
    ) {
        AppImage(
            imageUrl = imageData?.url,
            modifier = Modifier
                .border(
                    width = width.dp1,
                    color = colorTheme.bg.stone100,
                    shape = CircleShape
                )
                .clip(CircleShape)
                .aspectRatio(1f)
                .background(
                    color = colorTheme.background,
                    shape = CircleShape
                ),
            contentScale = ContentScale.Fit,
            contentDescription = imageData?.altText,
            imageLoader = imageLoader
        )
        Text(
            modifier = Modifier.padding(top = spacing.dp8),
            text = catalog.title,
            style = typographyTheme.labelSmallXs,
            color = if (catalog.title == "New Arrivals")
                colorTheme.text.red700 else colorTheme.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}