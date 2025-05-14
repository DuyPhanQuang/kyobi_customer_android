package com.kyobi.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kyobi.composable.skeleton.SkeletonContainer
import com.kyobi.domain.model.DomainNetworkResult
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
import timber.log.Timber

data class LookbookItem(
    val id: String,
    val imageUrl: String, // URL của ảnh hoặc GIF
    val hashtag: String, // Hashtag của video
)

@Composable
fun HomeTab(
    navController: NavHostController,
    viewModel: HomeTabViewModel = hiltViewModel(),
    topPadding: Dp,
    bottomPadding: Dp,
) {
    val tag = "HomeTab"
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

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.onPrimary
    ) {
        Box(
            modifier = Modifier
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
                                height = MaterialTheme.kyobiTheme.height.dp356,
                            )
                        }
                        is DomainNetworkResult.Success -> {
                            if (banners.isNotEmpty()) {
                                HomeSectionBanner(
                                    banners = banners,
                                    imageLoader = imageLoader
                                )
                            } else {
                                Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
                            }
                        }
                        is DomainNetworkResult.Error -> {
                            Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
                        }
                    }
                }
                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
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
                            Timber.tag(tag).d("Check catalogs: $topCatalogs")
                            if (topCatalogs.isNotEmpty()) {
                                Box(modifier = Modifier.fillMaxWidth()) {
                                    HomeSectionTopCatalog(
                                        items = topCatalogs,
                                        imageLoader = imageLoader
                                    )
                                }
                            } else {
                                Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
                            }
                        }
                        is DomainNetworkResult.Error -> {
                            Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
                        }
                    }
                }
                item {
                    if (flashSaleData != null) {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            HomeSectionDeals(
                                flashSaleData = flashSaleData,
                                imageLoader = imageLoader
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
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
                                    modifier = Modifier.fillMaxWidth(),
                                    saleProducts = saleProducts,
                                    imageLoader = imageLoader,
                                )
                            } else {
                                Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
                            }
                        }
                        is DomainNetworkResult.Error -> {
                            Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
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
                                Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
                            }
                        }
                        is DomainNetworkResult.Error -> {
                            Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
                        }
                    }
                }
                item {
                    HomeSectionRecommendedProductsGridView(
                        modifier = Modifier.fillMaxWidth(),
                        imageLoader = imageLoader
                    )
                }
            }
            HomeSectionHeader(
                modifier = Modifier
                    .statusBarsPadding()
                    .zIndex(1f)
                    .fillMaxWidth()
                    .height(MaterialTheme.kyobiTheme.height.dp88)
                    .padding(
                        start = MaterialTheme.kyobiTheme.spacing.dp12,
                        end = MaterialTheme.kyobiTheme.spacing.dp12,
                        bottom = MaterialTheme.kyobiTheme.spacing.dp8,
                    )
                    .align(Alignment.TopStart),
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