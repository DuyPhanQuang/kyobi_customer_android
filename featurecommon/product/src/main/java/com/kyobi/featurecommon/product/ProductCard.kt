package com.kyobi.featurecommon.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.composable.space.XxsSpaceX
import com.kyobi.composable.utils.ColorUtils
import com.kyobi.domain.extension.toColorsOption
import com.kyobi.domain.extension.toFormattedOriginalPrice
import com.kyobi.domain.extension.toFormattedSalePrice
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

    Column(
        modifier = modifier
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(MaterialTheme.kyobiTheme.shapes.small)
                .aspectRatio(0.668f)
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
                    .padding(top = MaterialTheme.kyobiTheme.spacing.dp4),
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp6),
            ) {
                colorsOption.forEach { color ->
                    val colorValue = ColorUtils.getColorValue(color)
                    Box(
                        modifier = Modifier
                            .size(MaterialTheme.kyobiTheme.width.dp16)
                            .clip(CircleShape)
                            .border(
                                width = MaterialTheme.kyobiTheme.width.dp1,
                                color = MaterialTheme.kyobiTheme.colors.bg.stone300,
                                shape = CircleShape)
                            .background(color = MaterialTheme.kyobiTheme.colors.bg.white)
                            .padding(MaterialTheme.kyobiTheme.spacing.dp2),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(color = colorValue),
                        )
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        vertical = MaterialTheme.kyobiTheme.spacing.dp4),
            ) {
                Text(
                    text = product.toFormattedSalePrice,
                    style = MaterialTheme.typography.labelSmallXs,
                    color = MaterialTheme.kyobiTheme.colors.text.red700,
                )
                XxsSpaceX()
                Text(
                    text = product.toFormattedOriginalPrice,
                    style = MaterialTheme.typography.labelSmallXs.copy(
                        textDecoration = TextDecoration.LineThrough
                    ),
                    color = MaterialTheme.kyobiTheme.colors.text.neutral400,
                )
            }
        }
    }
}