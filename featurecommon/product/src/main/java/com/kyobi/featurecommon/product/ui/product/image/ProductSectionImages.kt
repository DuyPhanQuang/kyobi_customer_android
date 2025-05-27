package com.kyobi.featurecommon.product.ui.product.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.zIndex
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.domain.model.ShopifyImage
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphXs

@Composable
fun ProductSectionImages(
    images: List<ShopifyImage>,
    imageLoader: ImageLoader,
    aspectRatio: Float
) {
    val pagerState = rememberPagerState(pageCount = { images.size })

    val spacing = MaterialTheme.kyobiTheme.spacing
    val shapeTheme = MaterialTheme.kyobiTheme.shapes
    val colorTheme = MaterialTheme.kyobiTheme.colors
    val typographyTheme = MaterialTheme.kyobiTheme.typography

    Box(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        HorizontalPager(
            modifier = Modifier
                .zIndex(0f)
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
        Row(
            modifier = Modifier
                .zIndex(1f)
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(
                    end = spacing.dp12,
                    bottom = spacing.dp12),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .clip(shapeTheme.extraLarge)
                    .background(colorTheme.onBackground.copy(alpha = 0.1f))
                    .padding(
                        vertical = spacing.dp8,
                        horizontal = spacing.dp16),
                contentAlignment = Alignment.Center
            ) {
                val currentPage = pagerState.currentPage
                val totalPage = pagerState.pageCount
                Text(
                    text = "${currentPage + 1} / $totalPage",
                    style = typographyTheme.paragraphXs,
                    color = colorTheme.text.neutral600,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}