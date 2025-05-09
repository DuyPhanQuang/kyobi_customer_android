package com.kyobi.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kyobi.home.ui.tab.HomeRecommendedReel
import com.kyobi.home.ui.tab.HomeSectionBanner
import com.kyobi.home.ui.tab.HomeSectionDeals
import com.kyobi.home.ui.tab.HomeSectionHeader
import com.kyobi.home.ui.tab.HomeSectionTopCatalog
import com.kyobi.home.ui.tab.deals.flip_clock.Flipper
import com.kyobi.theme.kyobiTheme

data class LookbookItem(
    val id: String,
    val imageUrl: String, // URL của ảnh hoặc GIF
    val hashtag: String, // Hashtag của video
)

data class ProductItem(
    val id: String,
    val imageUrl: String,
    val price: String,
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

    val banners = viewModel.getBanners()
    val mockReels = viewModel.getRecommendedReels()
    val mockTopCatalogs = viewModel.getTopCatalog()
    val mockProductDeals = viewModel.getProductDeals()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = MaterialTheme.kyobiTheme.spacing.dp0,
                bottom = bottomPadding
            ),
        color = MaterialTheme.colorScheme.onPrimary
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .zIndex(0f)
                .fillMaxSize()
        ) {
            item {
                HomeSectionBanner(
                    banners = banners,
                    imageLoader = imageLoader
                )
            }
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HomeRecommendedReel(
                        items = mockReels,
                        imageLoader = imageLoader
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HomeSectionTopCatalog(
                        items = mockTopCatalogs,
                        imageLoader = imageLoader,
                    )
                }
            }
            item {
                Box(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    HomeSectionDeals(
                        items = mockProductDeals,
                        imageLoader = imageLoader,
                    )
                }
            }
        }
        Box(
            modifier = Modifier.zIndex(1f)
        ) {
            HomeSectionHeader(
                topPadding = topPadding,
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