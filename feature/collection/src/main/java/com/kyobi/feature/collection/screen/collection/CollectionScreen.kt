package com.kyobi.feature.collection.screen.collection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.kyobi.feature.collection.extension.toCollectionMenus
import com.kyobi.feature.collection.screen.tab.CollectionTabViewModel
import com.kyobi.feature.collection.ui.collection.menu.CollectionSectionMenu
import com.kyobi.feature.collection.ui.common.CollectionCommonSectionHeader
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.theme.Colors
import com.kyobi.theme.Dimension
import com.kyobi.theme.kyobiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    collectionTabViewModel: CollectionTabViewModel,
    viewModel: CollectionViewModel = hiltViewModel(),
    categoryId: String?,
    bottomPadding: Dp,
    ) {
    val tag = "CollectionScreen"
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

    val selectedCollectionId = uiState.selectedCollectionId
    val collectionMenus = uiState.collectionMenus

    LaunchedEffect(categoryId) {
        if (categoryId != null) {
            val categorySelected = collectionTabViewModel.getCategorySelected(categoryId)
            val initCollectionMenus = categorySelected?.toCollectionMenus() ?: return@LaunchedEffect
            viewModel.setCollectionMenus(initCollectionMenus)
        }
    }

    // Track scroll direction
    LaunchedEffect(currentVisibleItemIndex) {
        // scroll down
        if (currentVisibleItemIndex > lastVisibleItemIndex) {
            showCollectionSection = false
        }
        // scroll up
        if (currentVisibleItemIndex < lastVisibleItemIndex || currentVisibleItemIndex == 0) {
            showCollectionSection = true
            expandedMenuSection = false
        }
        lastVisibleItemIndex = currentVisibleItemIndex
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
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues
        ) {
            item {
                AnimatedVisibility(
                    modifier = Modifier
                        .padding(top = spacing.dp2)
                        .drawBehind {
                            val strokeWidth = Dimension.dp1.toPx()
                            val borderColor = Colors().stone100
                            drawLine(
                                color = borderColor,
                                start = Offset(0f, 0f),
                                end = Offset(size.width, 0f),
                                strokeWidth = strokeWidth
                            )
                        },
                    visible = showCollectionSection,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    CollectionSectionMenu(
                        collectionMenus = collectionMenus,
                        imageLoader = imageLoader,
                        expanded = expandedMenuSection,
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
    }
}