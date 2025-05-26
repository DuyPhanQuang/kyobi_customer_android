package com.kyobi.feature.collection.screen.tab

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.SubcategoryMenu
import com.kyobi.feature.collection.ui.tab.category.CollectionTabSectionCategory
import com.kyobi.feature.collection.ui.common.CollectionCommonSectionHeader
import com.kyobi.feature.collection.ui.tab.products.CollectionTabSectionProductsGridView
import com.kyobi.feature.collection.ui.tab.sub_category.CollectionTabSectionSubCategory
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.featurecommon.routes.RouteKey
import com.kyobi.featurecommon.routes.Routes
import com.kyobi.theme.Colors
import com.kyobi.theme.Dimension
import com.kyobi.theme.kyobiTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun CollectionTab(
    navController: NavController,
    viewModel: CollectionTabViewModel,
    authViewModel: AuthViewModel,
    bottomPadding: Dp,
) {
    val tag = "CollectionTab"
    val productListViewModel: CollectionTabProductListViewModel = hiltViewModel()
    val eventBus = viewModel.getCollectionTabEventBus()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imageLoader = viewModel.getImageLoader()
    val lazyListState = rememberLazyListState()
    val productLazyGridState = rememberLazyGridState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    var showCategorySection by remember { mutableStateOf(true) }
    var lastVisibleItemIndex by remember { mutableIntStateOf(0) }
    val currentVisibleItemIndex by remember { derivedStateOf { productLazyGridState.firstVisibleItemIndex } }
    var expandedCategorySection by remember { mutableStateOf(false) }

    val selectedCategoryId = uiState.selectedCategoryId
    val selectedSubCategoryId = uiState.selectedSubCategoryId
    val categoryMenus = when (val result = uiState.subMenusResult) {
        is DomainNetworkResult.Success -> result.data
        is DomainNetworkResult.Loading -> emptyList()
        is DomainNetworkResult.Error -> emptyList()
    }
    val subCategoryMenus: List<SubcategoryMenu> = if (selectedCategoryId == null) {
        categoryMenus
            .flatMap { category -> category.groups ?: emptyList() }
            .flatMap { group -> group.subcategories ?: emptyList() }
    } else {
        categoryMenus
            .filter { it.id == selectedCategoryId }
            .flatMap { category -> category.groups ?: emptyList() }
            .flatMap { group -> group.subcategories ?: emptyList() }
    }

    LaunchedEffect(eventBus) {
        productListViewModel.initWithEventBus(eventBus)
    }

    // Track scroll direction
    LaunchedEffect(currentVisibleItemIndex) {
        // scroll up behavior
        if (currentVisibleItemIndex > lastVisibleItemIndex) {
            showCategorySection = false
            if (expandedCategorySection) {
                expandedCategorySection = false
            }
        }
        // scroll down behavior
        if (currentVisibleItemIndex < lastVisibleItemIndex || currentVisibleItemIndex == 0) {
            showCategorySection = true
        }
        lastVisibleItemIndex = currentVisibleItemIndex
    }

    LaunchedEffect(productLazyGridState.isScrollInProgress) {
        if (productLazyGridState.isScrollInProgress) {
            // scroll up behavior
            snapshotFlow { productLazyGridState.firstVisibleItemScrollOffset }
                .collect { scrollOffset ->
                    if (scrollOffset > 0) {
                        if (expandedCategorySection) {
                            expandedCategorySection = false
                        }
                    }
                }
        }
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
    val spacing = MaterialTheme.kyobiTheme.spacing
    val height = MaterialTheme.kyobiTheme.height

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(spacing.dp0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorTheme.background,
                    titleContentColor = colorTheme.background,
                    scrolledContainerColor = colorTheme.background,
                ),
                title = {
                    CollectionCommonSectionHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(height.dp88)
                            .background(colorTheme.background)
                            .padding(
                                start = spacing.dp0,
                                end = spacing.dp14,
                                top = spacing.dp8,
                                bottom = spacing.dp8
                            ),
                        onSearchClick = {
                        },
                        onFavouritesClick = {
                        },
                        onCartClick = {
                        }
                    )
                },
            )
        },
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(colorTheme.background),
                state = lazyListState,
                contentPadding = paddingValues
            ) {
                item {
                    AnimatedVisibility(
                        visible = showCategorySection,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Box(
                            modifier = Modifier
                                .drawBehind {
                                    val strokeWidth = Dimension.dp1.toPx()
                                    val borderColor = Colors().stone100
                                    drawLine(
                                        color = borderColor,
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, 0f),
                                        strokeWidth = strokeWidth
                                    )
                                }
                        ) {
                            CollectionTabSectionCategory(
                                categories = categoryMenus,
                                imageLoader = imageLoader,
                                expanded = expandedCategorySection,
                                onAllClick = { expandedCategorySection = true },
                                onCollapseClick = { expandedCategorySection = false },
                                selectedCategoryId = selectedCategoryId,
                                onCategoryClick = { category ->
                                    viewModel.updateCategorySelected(category)
                                }
                            )
                        }
                    }
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillParentMaxHeight() // important
                            .drawBehind {
                                val strokeWidth = Dimension.dp1.toPx()
                                val borderColor = Colors().stone100
                                drawLine(
                                    color = borderColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    strokeWidth = strokeWidth
                                )
                            }
                    ) {
                        CollectionTabSectionSubCategory(
                            modifier = Modifier
                                .fillMaxWidth(0.25f)
                                .fillMaxHeight(),
                            bottomPadding = bottomPadding,
                            subCategories = subCategoryMenus,
                            selectedSubCategoryId = selectedSubCategoryId,
                            onItemClick = { subCategory ->
                                viewModel.updateSubCategorySelected(subCategory, categoryMenus)
                            }
                        )
                        CollectionTabSectionProductsGridView(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            imageLoader = imageLoader,
                            lazyGridState = productLazyGridState,
                            productListViewModel = productListViewModel,
                            bottomPadding = bottomPadding,
                            onSortClick = {},
                            onFilterClick = {
                                val route = Routes.Collection.getRoute(
                                    RouteKey.Collection.CATEGORY_ID to selectedCategoryId,
                                    RouteKey.Collection.SUB_CATEGORY_ID to selectedSubCategoryId
                                )
                                navController.navigate(route)
                            },
                            onProductClick = { product ->
                                val productId = product.id
                                if (productId.isNotEmpty()) {
                                    val route = Routes.Product.getRoute(
                                        RouteKey.Product.ID to product.id
                                    )
                                    navController.navigate(route)
                                }
                            }
                        )
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = isRefreshing,
                state = pullRefreshState,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .zIndex(1f)
            )
        }
    }
}