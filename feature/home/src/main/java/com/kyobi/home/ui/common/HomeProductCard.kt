package com.kyobi.home.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.domain.model.Product
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallXs
import com.kyobi.theme.paragraphXs

@Composable
fun HomeProductCard(
    modifier: Modifier = Modifier,
    product: Product,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
) {
    val imageThumbnail = product.featuredImage

    Column(
        modifier = modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.668f)
                .clip(MaterialTheme.kyobiTheme.shapes.small)
        ) {
            AppImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.kyobiTheme.shapes.small),
                imageUrl = imageThumbnail?.url,
                contentDescription = imageThumbnail?.altText,
                imageLoader = imageLoader,
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        vertical = MaterialTheme.kyobiTheme.spacing.dp8,
                        horizontal = MaterialTheme.kyobiTheme.spacing.dp8
                    )
            ) {
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.kyobiTheme.colors.bg.red700)
                        .padding(
                            vertical = MaterialTheme.kyobiTheme.spacing.dp4,
                            horizontal = MaterialTheme.kyobiTheme.spacing.dp8
                        )
                ) {
                    Text(
                        text = "-20%",
                        style = MaterialTheme.typography.labelSmallXs,
                        color = MaterialTheme.kyobiTheme.colors.text.white,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                top = MaterialTheme.kyobiTheme.spacing.dp8,
                start = MaterialTheme.kyobiTheme.spacing.dp8,
                end = MaterialTheme.kyobiTheme.spacing.dp8,
            )
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = product.title,
                style = MaterialTheme.typography.paragraphXs,
                color = MaterialTheme.kyobiTheme.colors.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                    vertical = MaterialTheme.kyobiTheme.spacing.dp4),
            ) {
                Text(
                    text = "${product.priceRange.minVariantPrice.currencyCode} ${product.priceRange.minVariantPrice.amount}",
                    style = MaterialTheme.typography.labelSmallXs,
                    color = MaterialTheme.kyobiTheme.colors.text.red700,
                )
                XxsSpaceX()
                Text(
                    text = "${product.compareAtPriceRange.minVariantPrice.currencyCode} ${product.compareAtPriceRange.minVariantPrice.amount}",
                    style = MaterialTheme.typography.labelSmallXs.copy(
                        textDecoration = TextDecoration.LineThrough
                    ),
                    color = MaterialTheme.kyobiTheme.colors.text.neutral400,
                )
            }
        }
    }
}