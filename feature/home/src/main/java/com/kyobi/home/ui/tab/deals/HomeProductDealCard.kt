package com.kyobi.home.ui.tab.deals

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.kyobi.home.ProductItem
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.labelXs

@Composable
fun HomeProductDealCard(
    product: ProductItem,
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
                .height(MaterialTheme.kyobiTheme.height.dp156)
        ) {
            AppImage(
                modifier = Modifier.fillMaxSize(),
                imageUrl = product.imageUrl,
                contentDescription = "Product Image",
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
                    text = "$${product.price}",
                    color = MaterialTheme.kyobiTheme.colors.bg.red600,
                    style = MaterialTheme.kyobiTheme.typography.labelXs,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}