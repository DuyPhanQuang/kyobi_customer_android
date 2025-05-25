package com.kyobi.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kyobi.composable.skeleton.SkeletonContainer
import com.kyobi.composable.space.SpaceY
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.home.ui.tab.reels.HomeRecommendedReel
import com.kyobi.home.ui.tab.banners.HomeSectionBanner
import com.kyobi.home.ui.tab.deals.HomeSectionDeals
import com.kyobi.home.ui.tab.HomeSectionHeader
import com.kyobi.home.ui.tab.recommended_products.HomeSectionRecommendedProductsGridView
import com.kyobi.home.ui.tab.sale_products.HomeSectionSaleProducts
import com.kyobi.home.ui.tab.sale_products.SkeletonSaleProductGridView
import com.kyobi.home.ui.tab.spotlights.HomeSectionSpotlights
import com.kyobi.home.ui.tab.spotlights.SkeletonSpotlightGridView
import com.kyobi.home.ui.tab.top_catalogs.HomeSectionTopCatalog
import com.kyobi.home.ui.tab.top_catalogs.SkeletonTopCatalogGridView
import com.kyobi.theme.kyobiTheme

data class LookbookItem(
    val id: String,
    val imageUrl: String, // URL của ảnh hoặc GIF
    val hashtag: String, // Hashtag của video
)

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeTab(
    navController: NavHostController,
    viewModel: HomeTabViewModel,
    authViewModel: AuthViewModel,
    topPadding: Dp,
    bottomPadding: Dp,
) {
    val tag = "HomeTab"
    val productListViewModel: HomeRecommendedProductListViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val imageLoader = viewModel.getImageLoader()

    val mockReels = viewModel.getRecommendedReels()

    val banners = when (val result = uiState.bannersResult) {
        is DomainNetworkResult.Success -> result.data
        is DomainNetworkResult.Loading -> emptyList()
        is DomainNetworkResult.Error -> emptyList()
    }

    val topCatalogs = when (val result = uiState.topCatalogsResult) {
        is DomainNetworkResult.Success -> result.data
        is DomainNetworkResult.Loading -> emptyList()
        is DomainNetworkResult.Error -> emptyList()
    }

    val flashSaleData = when (val result = uiState.flashSaleResult) {
        is DomainNetworkResult.Success -> result.data
        is DomainNetworkResult.Loading -> null
        is DomainNetworkResult.Error -> null
    }

    val saleProducts = when (val result = uiState.saleProductsResult) {
        is DomainNetworkResult.Success -> result.data
        is DomainNetworkResult.Loading -> emptyList()
        is DomainNetworkResult.Error -> emptyList()
    }

    val trendingResearchs = when (val result = uiState.trendingResearchResult) {
        is DomainNetworkResult.Success -> result.data
        is DomainNetworkResult.Loading -> emptyList()
        is DomainNetworkResult.Error -> emptyList()
    }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.onRefreshTriggered {
                isRefreshing = false
            }
        }
    )

    val colorTheme = MaterialTheme.kyobiTheme.colors
    val height = MaterialTheme.kyobiTheme.height
    val spacing = MaterialTheme.kyobiTheme.spacing

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = colorTheme.onPrimary
    ) {
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .zIndex(0f)
                    .fillMaxWidth()
                    .wrapContentHeight(),
                contentPadding = PaddingValues(bottom = bottomPadding)
            ) {
                item {
                    when (uiState.bannersResult) {
                        is DomainNetworkResult.Loading -> {
                            SkeletonContainer(
                                modifier = Modifier.fillMaxWidth(),
                                height = height.dp356,
                            )
                        }
                        is DomainNetworkResult.Success -> {
                            if (banners.isNotEmpty()) {
                                HomeSectionBanner(
                                    banners = banners,
                                    imageLoader = imageLoader
                                )
                            } else {
                                spacing.dp0.SpaceY()
                            }
                        }
                        is DomainNetworkResult.Error -> {
                            spacing.dp0.SpaceY()
                        }
                    }
                }
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        HomeRecommendedReel(
                            items = mockReels,
                            imageLoader = imageLoader
                        )
                    }
                }
                item {
                    when (uiState.topCatalogsResult) {
                        is DomainNetworkResult.Loading -> {
                            SkeletonTopCatalogGridView(modifier = Modifier.fillMaxWidth())
                        }
                        is DomainNetworkResult.Success -> {
                            if (topCatalogs.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    HomeSectionTopCatalog(
                                        items = topCatalogs,
                                        imageLoader = imageLoader
                                    )
                                }
                            } else {
                                spacing.dp0.SpaceY()
                            }
                        }
                        is DomainNetworkResult.Error -> {
                            spacing.dp0.SpaceY()
                        }
                    }
                }
                item {
                    if (flashSaleData != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                        ) {
                            HomeSectionDeals(
                                flashSaleData = flashSaleData,
                                imageLoader = imageLoader
                            )
                        }
                    } else {
                        spacing.dp0.SpaceY()
                    }
                }
                item {
                    when (uiState.saleProductsResult) {
                        is DomainNetworkResult.Loading -> {
                            SkeletonSaleProductGridView(modifier = Modifier.fillMaxWidth())
                        }
                        is DomainNetworkResult.Success -> {
                            if (saleProducts.isNotEmpty()) {
                                HomeSectionSaleProducts(
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    saleProducts = saleProducts,
                                    imageLoader = imageLoader,
                                )
                            } else {
                                spacing.dp0.SpaceY()
                            }
                        }
                        is DomainNetworkResult.Error -> {
                            spacing.dp0.SpaceY()
                        }
                    }
                }
                item {
                    when (uiState.trendingResearchResult) {
                        is DomainNetworkResult.Loading -> {
                            SkeletonSpotlightGridView(modifier = Modifier.fillMaxWidth())
                        }
                        is DomainNetworkResult.Success -> {
                            if (trendingResearchs.isNotEmpty()) {
                                HomeSectionSpotlights(
                                    items = trendingResearchs,
                                    imageLoader = imageLoader,
                                )
                            } else {
                                spacing.dp0.SpaceY()
                            }
                        }
                        is DomainNetworkResult.Error -> {
                            spacing.dp0.SpaceY()
                        }
                    }
                }
                item {
                    HomeSectionRecommendedProductsGridView(
                        modifier = Modifier
                            .fillMaxWidth(),
                        productListViewModel = productListViewModel,
                        imageLoader = imageLoader
                    )
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
            )
            HomeSectionHeader(
                modifier = Modifier
                    .statusBarsPadding()
                    .zIndex(1f)
                    .fillMaxWidth()
                    .height(height.dp88)
                    .align(Alignment.TopStart)
                    .padding(
                        start = spacing.dp12,
                        end = spacing.dp12,
                        bottom = spacing.dp8),
                onSearchClick = {
                },
                onFavouritesClick = {
                },
                onCartClick = {
                }
            )
        }
    }
}