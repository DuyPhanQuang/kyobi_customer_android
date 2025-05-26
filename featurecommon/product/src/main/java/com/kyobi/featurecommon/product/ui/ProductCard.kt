package com.kyobi.featurecommon.product.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import com.kyobi.composable.product_option.ColorOption
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.composable.utils.ColorUtils
import com.kyobi.domain.extension.toColorsOption
import com.kyobi.domain.extension.toFormattedOriginalPrice
import com.kyobi.domain.extension.toFormattedSalePrice
import com.kyobi.featurecommon.product.ProductUiState
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelSmallXs
import com.kyobi.theme.paragraphXs

@Composable
fun ProductCard(
    modifier: Modifier = Modifier,
    productUiState: ProductUiState,
    imageLoader: ImageLoader,
    onClick: () -> Unit,
) {
    val product = productUiState.product
    val imageThumbnail = product.featuredImage
    val colorsOption = product.toColorsOption

    val shapesTheme = MaterialTheme.kyobiTheme.shapes
    val spacing = MaterialTheme.kyobiTheme.spacing
    val typographyTheme = MaterialTheme.kyobiTheme.typography
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val width = MaterialTheme.kyobiTheme.width

    Column(
        modifier = modifier
            .clip(shapesTheme.small)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shapesTheme.small)
                .aspectRatio(0.668f)
        ) {
            AppImage(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(shapesTheme.small),
                imageUrl = imageThumbnail?.url,
                contentDescription = imageThumbnail?.altText,
                imageLoader = imageLoader,
                contentScale = ContentScale.Crop,
            )
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(
                        vertical = spacing.dp8,
                        horizontal = spacing.dp8)
            ) {
                Box(
                    modifier = Modifier
                        .background(colorTheme.bg.red700)
                        .padding(
                            vertical = spacing.dp4,
                            horizontal = spacing.dp8)
                ) {
                    Text(
                        text = "-20%",
                        style = typographyTheme.labelSmallXs,
                        color = colorTheme.text.white,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = spacing.dp8,
                    start = spacing.dp8,
                    end = spacing.dp8)
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth(),
                text = product.title,
                style = typographyTheme.paragraphXs,
                color = colorTheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = spacing.dp4),
                horizontalArrangement = Arrangement.spacedBy(spacing.dp6),
            ) {
                colorsOption.forEach { color ->
                    val colorValue = ColorUtils.getColorValue(color)
                    ColorOption(
                        color = colorValue,
                        size = width.dp16,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = spacing.dp4),
            ) {
                Text(
                    text = product.toFormattedSalePrice,
                    style = typographyTheme.labelSmallXs,
                    color = colorTheme.text.red700,
                )
                XxsSpaceX()
                Text(
                    text = product.toFormattedOriginalPrice,
                    style = typographyTheme.labelSmallXs.copy(
                        textDecoration = TextDecoration.LineThrough),
                    color = colorTheme.text.neutral400,
                )
            }
        }
    }
}