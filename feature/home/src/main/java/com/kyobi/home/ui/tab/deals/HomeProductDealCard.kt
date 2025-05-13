package com.kyobi.home.ui.tab.deals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.domain.extension.toFormattedSalePrice
import com.kyobi.domain.model.Product
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelXs

@Composable
fun HomeProductDealCard(
    product: Product,
    imageLoader: ImageLoader
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(MaterialTheme.kyobiTheme.width.dp120)
            .clip(MaterialTheme.kyobiTheme.shapes.extraSmall)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.7692f)
        ) {
            val imageData = product.featuredImage
            AppImage(
                modifier = Modifier.fillMaxSize(),
                imageUrl = imageData?.url,
                contentDescription = imageData?.altText,
                imageLoader = imageLoader
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.kyobiTheme.colors.bg.white)
                    .padding(vertical = MaterialTheme.kyobiTheme.spacing.dp4)
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = product.toFormattedSalePrice,
                    color = MaterialTheme.kyobiTheme.colors.bg.red600,
                    style = MaterialTheme.kyobiTheme.typography.labelXs,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}