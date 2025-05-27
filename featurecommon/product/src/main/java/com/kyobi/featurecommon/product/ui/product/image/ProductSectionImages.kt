package com.kyobi.featurecommon.product.ui.product.image

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.domain.model.ShopifyImage
import com.kyobi.theme.kyobiTheme

@Composable
fun ProductSectionImages(
    images: List<ShopifyImage>,
    imageLoader: ImageLoader,
    aspectRatio: Float
) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    val spacing = MaterialTheme.kyobiTheme.spacing

    HorizontalPager(
        modifier = Modifier
            .fillMaxSize(),
        state = pagerState,
    ) { page ->
        val imageData = images[page]
        AppImage(
            modifier = Modifier.aspectRatio(aspectRatio),
            imageUrl = imageData.url,
            contentDescription = imageData.altText,
            imageLoader = imageLoader,
            contentScale = ContentScale.Crop
        )
    }
}