package com.kyobi.feature.catalog.ui.tab.category

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.kyobi.theme.paragraphXs

@Composable
fun CategoryTile(
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
        AppImage(
            imageUrl = imageData?.url,
            modifier = Modifier
                .size(MaterialTheme.kyobiTheme.width.dp48)
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
            contentDescription = imageData?.altText,
            imageLoader = imageLoader
        )
        Text(
            modifier = Modifier.padding(top = MaterialTheme.kyobiTheme.spacing.dp4),
            text = catalog.title,
            style = MaterialTheme.kyobiTheme.typography.paragraphXs,
            color = if (catalog.title == "New Arrivals")
                MaterialTheme.kyobiTheme.colors.text.red700 else
                MaterialTheme.kyobiTheme.colors.onBackground,
            textAlign = TextAlign.Center,
        )
    }
}