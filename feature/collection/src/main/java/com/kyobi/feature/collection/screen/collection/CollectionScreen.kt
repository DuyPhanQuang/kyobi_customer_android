package com.kyobi.feature.collection.screen.collection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kyobi.feature.collection.extension.toCollectionMenu
import com.kyobi.feature.collection.extension.toCollectionMenus
import com.kyobi.feature.collection.screen.tab.CollectionTabViewModel
import com.kyobi.feature.collection.ui.collection.menu.CollectionSectionMenu
import com.kyobi.feature.collection.ui.collection.products.CollectionSectionProductsGridView
import com.kyobi.feature.collection.ui.collection.sort_filter.CollectionSectionSortFilter
import com.kyobi.feature.collection.ui.collection.sort_filter.CollectionSectionSortFilterType
import com.kyobi.feature.collection.ui.collection.sort_filter.GridViewModeType
import com.kyobi.feature.collection.ui.common.CollectionCommonSectionHeader
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.theme.Colors
import com.kyobi.theme.Dimension
import com.kyobi.theme.kyobiTheme
import timber.log.Timber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    collectionTabViewModel: CollectionTabViewModel,
    categoryId: String?,
    subCategoryId: String?,
    bottomPadding: Dp
) {
    val tag = "CollectionScreen"
    val eventBus = remember { CollectionScreenEventBus() }
    val viewModel: CollectionScreenViewModel = hiltViewModel()
    val productListViewModel: CollectionScreenProductListViewModel = hiltViewModel()

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val imageLoader = viewModel.getImageLoader()
    val lazyListState = rememberLazyListState()
    val productLazyGridState = rememberLazyGridState()

    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    var showCollectionSection by remember { mutableStateOf(true) }
    var lastVisibleItemIndex by remember { mutableIntStateOf(0) }
    val currentVisibleItemIndex by remember { derivedStateOf { productLazyGridState.firstVisibleItemIndex } }
    var expandedMenuSection by remember { mutableStateOf(false) }

    var showDropdown by remember { mutableStateOf<CollectionSectionSortFilterType?>(null) }
    var gridViewMode by remember { mutableStateOf(GridViewModeType.COLUMNS_2) }

    val selectedCollectionId = uiState.selectedCollectionId
    val collectionMenus = uiState.collectionMenus
    val cateFilter = uiState.cateFilter

    LaunchedEffect(eventBus) {
        viewModel.initWithEventBus(eventBus)
        productListViewModel.initWithEventBus(eventBus)
    }

    LaunchedEffect(Unit) {
        if (categoryId == null) {
            val categories = collectionTabViewModel.getCategories()
            if (!categories.isNullOrEmpty()) {
                val categoriesAsCollectionMenus = categories.toCollectionMenus()
                viewModel.setCollectionMenus(categoriesAsCollectionMenus)
                viewModel.updateNonCollectionSelect()
            }
        } else {
            val categorySelected = collectionTabViewModel.getCategorySelected(categoryId) ?: return@LaunchedEffect
            val subCategorySelected = collectionTabViewModel.getSubCategorySelected(subCategoryId)
            val initCollectionMenus = categorySelected.toCollectionMenus()
            viewModel.setCollectionMenus(initCollectionMenus)
            if (subCategorySelected == null) {
                val categorySelectedAsCollectionMenu = categorySelected.toCollectionMenu()
                viewModel.updateCollectionSelected(categorySelectedAsCollectionMenu)
            } else {
                val subCategorySelectedAsCollectionMenu = subCategorySelected.toCollectionMenu()
                viewModel.updateCollectionSelected(subCategorySelectedAsCollectionMenu)
            }
        }
    }

    // Track scroll direction
    LaunchedEffect(currentVisibleItemIndex) {
        // scroll up behavior
        if (currentVisibleItemIndex > lastVisibleItemIndex) {
            showCollectionSection = false
            if (expandedMenuSection) {
                expandedMenuSection = false
            }
        }
        // scroll down behavior
        if (currentVisibleItemIndex < lastVisibleItemIndex || currentVisibleItemIndex == 0) {
            showCollectionSection = true
        }
        lastVisibleItemIndex = currentVisibleItemIndex
    }

    LaunchedEffect(productLazyGridState.isScrollInProgress) {
        if (productLazyGridState.isScrollInProgress) {
            // scroll up behavior
            snapshotFlow { productLazyGridState.firstVisibleItemScrollOffset }
                .collect { scrollOffset ->
                    if (scrollOffset > 0) {
                        if (expandedMenuSection) {
                            expandedMenuSection = false
                        }
                    }
                }
        }
    }

    val colorTheme = MaterialTheme.kyobiTheme.colors
    val spacing = MaterialTheme.kyobiTheme.spacing

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(MaterialTheme.kyobiTheme.width.dp0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = colorTheme.background,
                    titleContentColor = colorTheme.background,
                    scrolledContainerColor = colorTheme.background,
                ),
                title = {
                    CollectionCommonSectionHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MaterialTheme.kyobiTheme.height.dp88)
                            .background(colorTheme.background)
                            .padding(
                                start = spacing.dp0,
                                end = spacing.dp14,
                                top = spacing.dp8,
                                bottom = spacing.dp8
                            ),
                        showBackIcon = true,
                        onBackClick = {
                            navController.popBackStack()
                        },
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
        LazyColumn(
            state = lazyListState,
            modifier = Modifier
                .fillMaxSize()
                .background(colorTheme.background),
            contentPadding = paddingValues
        ) {
            item {
                AnimatedVisibility(
                    visible = showCollectionSection,
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
                        CollectionSectionMenu(
                            collectionMenus = collectionMenus,
                            imageLoader = imageLoader,
                            expanded = expandedMenuSection,
                            enabledAll = showDropdown == null,
                            onAllClick = { expandedMenuSection = true },
                            onCollapseClick = { expandedMenuSection = false },
                            selectedCollectionId = selectedCollectionId,
                            onMenuItemClick = { collectionMenu ->
                                viewModel.updateCollectionSelected(collectionMenu)
                            }
                        )
                    }
                }
            }
            stickyHeader {
                CollectionSectionSortFilter(
                    modifier = Modifier
                        .fillMaxWidth()
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
                        .padding(
                            vertical = spacing.dp12,
                            horizontal = spacing.dp12),
                    cateFilter = cateFilter,
                    showDropdown = showDropdown,
                    updateShowDropdown = { newType ->
                        showDropdown = newType
                    },
                    onSortClick = {
                        Timber.tag(tag).d("onSortClick")
                    },
                    onColorFilterClick = {
                        Timber.tag(tag).d("onColorFilterClick")
                    },
                    onSizeFilterClick = {
                        Timber.tag(tag).d("onSizeFilterClick")
                    },
                    viewMode = gridViewMode,
                    onFilterAllClick = {},
                    onViewModeClick = { viewMode ->
                        gridViewMode = viewMode
                    }
                )
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
                    CollectionSectionProductsGridView(
                        modifier = Modifier
                            .fillMaxSize(),
                        imageLoader = imageLoader,
                        lazyGridState = productLazyGridState,
                        productListViewModel = productListViewModel,
                        bottomPadding = bottomPadding,
                    )
                }
            }
        }
    }
}