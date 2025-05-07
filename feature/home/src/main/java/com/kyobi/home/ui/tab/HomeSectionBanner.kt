package com.kyobi.home.ui.tab

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.kyobi.composable.space.XsSpaceX
import com.kyobi.home.ui.animate.animateShapeAsState
import com.kyobi.theme.kyobiTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeSectionBanner() {
    val banners = listOf(
        "https://images.unsplash.com/photo-1506157786151-b8491531f063",
        "https://images.unsplash.com/photo-1511556820780-d912e42b4980",
        "https://images.unsplash.com/photo-1483985988355-763728e1935b"
    )

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
                        animationSpec = tween(durationMillis = 500))
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.kyobiTheme.height.dp320)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                AsyncImage(
                    model = banners[page],
                    contentDescription = "Banner Item $page",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(
                        bottom = MaterialTheme.kyobiTheme.spacing.dp8
                    ),
                horizontalArrangement = Arrangement.Center
            ) {
                banners.forEachIndexed { index, _ ->
                    val isSelected = index == pagerState.currentPage
                    val targetWidth by animateDpAsState(
                        targetValue = if (isSelected)
                            MaterialTheme.kyobiTheme.width.dp24 else
                            MaterialTheme.kyobiTheme.width.dp8,
                        animationSpec = tween(durationMillis = 300),
                        label = "DotWidthAnimation"
                    )
                    val targetShape by animateShapeAsState(
                        targetValue = if (isSelected)
                            MaterialTheme.kyobiTheme.shapes.extraSmall else
                                CircleShape,
                        animationSpec = tween(durationMillis = 300)
                    )

                    Box(
                        modifier = Modifier
                            .size(
                                width = targetWidth,
                                height = MaterialTheme.kyobiTheme.width.dp8
                            )
                            .clip(targetShape)
                            .background(if (isSelected)
                                MaterialTheme.kyobiTheme.colors.bg.logo else
                                    MaterialTheme.kyobiTheme.colors.bg.white)
                            .clickable {
                                isUserInteracting = true
                                coroutineScope.launch {
                                    val targetPage = if (index == 0 && pagerState.currentPage == banners.size - 1) {
                                        0
                                    } else {
                                        index
                                    }
                                    pagerState.animateScrollToPage(targetPage)
                                    isUserInteracting = false
                                }
                            }
                    )
                    XsSpaceX()
                }
            }
        }
    }
}
