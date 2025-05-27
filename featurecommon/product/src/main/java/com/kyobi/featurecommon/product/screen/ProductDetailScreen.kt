package com.kyobi.featurecommon.product.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kyobi.composable.image.getImageHeightByAspectRatio
import com.kyobi.domain.extension.toUniqueAllProductImages
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.featurecommon.product.ProductDetailViewModel
import com.kyobi.featurecommon.product.ui.product.header.PinnedHeaderMenuBarType
import com.kyobi.featurecommon.product.ui.product.header.ProductSectionHeader
import com.kyobi.featurecommon.product.ui.product.header.ProductSectionPinnedHeader
import com.kyobi.featurecommon.product.ui.product.image.ProductSectionImages
import com.kyobi.theme.kyobiTheme

@Composable
fun ProductDetailScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    productId: String,
    initProduct: Product? = null,
    topPadding: Dp,
    bottomPadding: Dp
) {
    val viewModel: ProductDetailViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imageLoader = viewModel.getImageLoader()

    val lazyListState = rememberLazyListState()

    val heightTheme = MaterialTheme.kyobiTheme.height
    val colorTheme = MaterialTheme.kyobiTheme.colors

    val imageAspectRatio = 0.7090f
    val imageHeightInDp = getImageHeightByAspectRatio(imageAspectRatio)
    val parallaxOffset by remember {
        derivedStateOf {
            (lazyListState.firstVisibleItemScrollOffset * 0.5f).coerceAtMost(imageHeightInDp.value)
        }
    }

    val normalHeaderHeight = heightTheme.dp48 + topPadding
    val pinnedHeaderHeight = heightTheme.dp96 + topPadding
    val thresholdHeaderVisible = imageHeightInDp / 10 // ngưỡng attached pinned header 10% image height
    val showScrolledHeader by remember {
        derivedStateOf {
            lazyListState.firstVisibleItemIndex > 0 ||
            lazyListState.firstVisibleItemScrollOffset.toFloat() > thresholdHeaderVisible.value
        }
    }
    val scrollOffset by remember {
        derivedStateOf {
            val index = lazyListState.firstVisibleItemIndex
            val offset = lazyListState.firstVisibleItemScrollOffset
            // Approximate total scroll offset
            if (index == 0) offset.toFloat() else (imageHeightInDp.value + offset)
        }
    }
    val maxScrollForEffect = imageHeightInDp.value / 2 // 50% image height
    val alphaValue = (scrollOffset / maxScrollForEffect).coerceIn(0f, 1f)
    val pinnedHeaderBgColor by animateColorAsState(
        targetValue = colorTheme.background.copy(alpha = alphaValue),
        animationSpec = tween(durationMillis = 300)
    )

    val productData = when (val result = uiState.productResult) {
        is DomainNetworkResult.Success -> result.data
        is DomainNetworkResult.Loading -> null
        is DomainNetworkResult.Error -> null
    }
    val productImages = productData?.toUniqueAllProductImages() ?: emptyList()

    LaunchedEffect(initProduct) {
        if (initProduct != null) {
            viewModel.setInitProduct(initProduct)
        }
    }

    val spacing = MaterialTheme.kyobiTheme.spacing

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        LazyColumn(
            modifier = Modifier
                .zIndex(0f)
                .fillMaxSize(),
            state = lazyListState,
            contentPadding = PaddingValues(bottom = bottomPadding),
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    if (productImages.isNotEmpty()) {
                        Box(
                            modifier = Modifier
                                .zIndex(0f)
                                .fillMaxWidth()
                                .height(imageHeightInDp)
                                .graphicsLayer {
                                    translationY = parallaxOffset
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            ProductSectionImages(
                                imageLoader = imageLoader,
                                images = productImages,
                                aspectRatio = imageAspectRatio
                            )
                        }
                    }
                    AnimatedVisibility(
                        modifier = Modifier
                            .zIndex(1f)
                            .fillMaxWidth()
                            .height(normalHeaderHeight)
                            .padding(top = topPadding),
                        visible = !showScrolledHeader,
                        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                        exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut()
                    ) {
                        ProductSectionHeader(
                            modifier = Modifier.fillMaxSize(),
                            onBackClick = {},
                            onFavouriteClick = {},
                            onSearchClick = {},
                            onShareClick = {},
                        )
                    }
                }
            }
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(600.dp)
                        .background(Color.Blue)
                )
            }
            item {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .background(Color.LightGray)
                )
            }
        }
        AnimatedVisibility(
            modifier = Modifier
                .zIndex(1f)
                .fillMaxWidth()
                .height(pinnedHeaderHeight)
                .align(Alignment.TopCenter)
                .background(pinnedHeaderBgColor)
                .padding(top = topPadding),
            visible = showScrolledHeader,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ProductSectionPinnedHeader(
                modifier = Modifier.fillMaxSize(),
                menuBarType = PinnedHeaderMenuBarType.OVERVIEW,
                onBackClick = {},
                onFavouriteClick = {},
                onSearchClick = {},
                onShareClick = {},
                onMenuBarClick = {}
            )
        }
    }
}