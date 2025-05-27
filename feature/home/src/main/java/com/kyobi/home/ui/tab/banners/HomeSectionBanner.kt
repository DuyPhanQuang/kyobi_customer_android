package com.kyobi.home.ui.tab.banners

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.zIndex
import coil.ImageLoader
import com.kyobi.composable.image.AppImage
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.domain.model.Banner
import com.kyobi.theme.kyobiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeSectionBanner(
    banners: List<Banner>,
    imageLoader: ImageLoader
) {
    val pagerState = rememberPagerState(pageCount = { banners.size })
    val coroutineScope = rememberCoroutineScope()
    var isUserInteracting by remember { mutableStateOf(false) }

    LaunchedEffect(pagerState.currentPage) {
        if (!isUserInteracting) {
            while (true) {
                delay(5000)
                val nextPage = (pagerState.currentPage + 1) % banners.size
                coroutineScope.launch {
                    pagerState.animateScrollToPage(
                        nextPage,
                        animationSpec = tween(durationMillis = 300))
                }
            }
        }
    }

    val width = MaterialTheme.kyobiTheme.width
    val height = MaterialTheme.kyobiTheme.height
    val spacing = MaterialTheme.kyobiTheme.spacing
    val colorTheme = MaterialTheme.kyobiTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp356)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            HorizontalPager(
                modifier = Modifier
                    .zIndex(0f)
                    .fillMaxSize(),
                state = pagerState,
            ) { page ->
                val imageData = banners[page].image?.image
                if (imageData != null) {
                    AppImage(
                        modifier = Modifier.fillMaxSize(),
                        imageUrl = imageData.url,
                        contentDescription = imageData.altText,
                        imageLoader = imageLoader
                    )
                } else {
                    Box {
                        Spacer(modifier = Modifier)
                    }
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = spacing.dp36 + spacing.dp8),
                horizontalArrangement = Arrangement.Center
            ) {
                banners.forEachIndexed { index, _ ->
                    val isSelected = index == pagerState.currentPage
                    val targetWidth by animateDpAsState(
                        targetValue = if (isSelected) width.dp24 else width.dp8,
                        animationSpec = tween(durationMillis = 300),
                        label = "DotWidthAnimation"
                    )
                    Box(
                        modifier = Modifier
                            .size(width = targetWidth, height = width.dp8)
                            .clip(CircleShape)
                            .background(colorTheme.bg.stone100)
                            .clickable {
                                isUserInteracting = true
                                coroutineScope.launch {
                                    val isLast = pagerState.currentPage == banners.size - 1 && index == 0
                                    val targetPage = if (isLast) 0 else index
                                    pagerState.animateScrollToPage(targetPage)
                                    isUserInteracting = false
                                }
                            }
                    )
                    XsSpaceX()
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                HomeSectionVoucher()
            }
        }
    }
}
