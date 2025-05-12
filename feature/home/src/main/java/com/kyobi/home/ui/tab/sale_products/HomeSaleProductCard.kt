package com.kyobi.home.ui.tab.sale_products

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.domain.extension.toFormattedSalePrice
import com.kyobi.domain.model.Product
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.smallTitle

@Composable
fun HomeSaleProductCard(
    modifier: Modifier = Modifier,
    product: Product,
    imageLoader: ImageLoader,
) {
    val imageData = product.featuredImage
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        AppImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.8324f)
                .clip(MaterialTheme.kyobiTheme.shapes.extraSmall),
            imageUrl = imageData?.url,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop
        )
        Text(
            modifier = Modifier.padding(vertical = MaterialTheme.kyobiTheme.spacing.dp4),
            text = product.toFormattedSalePrice,
            style = MaterialTheme.kyobiTheme.typography.smallTitle,
            color = MaterialTheme.kyobiTheme.colors.onBackground
        )
    }
}