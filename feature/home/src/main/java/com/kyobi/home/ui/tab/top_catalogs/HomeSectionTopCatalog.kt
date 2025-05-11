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

    Column(
        modifier = Modifier.padding(
            bottom = MaterialTheme.kyobiTheme.spacing.dp8,
        )
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = MaterialTheme.kyobiTheme.spacing.dp8),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp12)
            ) {
                rowItems.forEach { catalog ->
                    TopCatalogTile(
                        modifier = Modifier.weight(1f), // Chia đều chiều rộng cho mỗi item
                        catalog = catalog,
                        imageLoader = imageLoader,
                        onItemClick = {  },
                    )
                }
                // Nếu hàng không đủ 5 item, thêm khoảng trống để giữ layout đồng đều
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
        modifier = modifier
            .padding(
                top = MaterialTheme.kyobiTheme.spacing.dp8
            )
            .clickable { onItemClick() }
    ) {
        if (imageData != null) {
            AppImage(
                imageUrl = imageData.url,
                modifier = Modifier
                    .clip(CircleShape)
                    .border(
                        width = MaterialTheme.kyobiTheme.width.dp1,
                        color = MaterialTheme.kyobiTheme.colors.bg.stone100,
                        shape = CircleShape
                    )
                    .aspectRatio(1f)
                    .background(
                        color = MaterialTheme.kyobiTheme.colors.background,
                        shape = CircleShape
                    ),
                contentScale = ContentScale.Fit,
                contentDescription = imageData.altText,
                imageLoader = imageLoader
            )
        }
        Text(
            modifier = Modifier.padding(top = MaterialTheme.kyobiTheme.spacing.dp8),
            text = catalog.title,
            style = MaterialTheme.kyobiTheme.typography.labelSmallXs,
            color = if (catalog.title == "New Arrivals")
                MaterialTheme.kyobiTheme.colors.text.red700 else
                MaterialTheme.kyobiTheme.colors.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}