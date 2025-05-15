package com.kyobi.feature.catalog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.model.TopCatalogStatus
import com.kyobi.feature.catalog.ui.tab.category.CatalogSectionCategory
import com.kyobi.feature.catalog.ui.tab.CatalogSectionHeader
import com.kyobi.feature.catalog.ui.tab.CatalogSectionProductsGridView
import com.kyobi.feature.catalog.ui.tab.CatalogSectionSubCategory
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.theme.kyobiTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogTab(
    navController: NavController,
    viewModel: CatalogTabViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    topPadding: Dp,
    bottomPadding: Dp,
) {
    val imageLoader = viewModel.getImageLoader()
    val lazyListState = rememberLazyListState()
    val subCategoryLazyListState = rememberLazyListState()
    val productLazyListState = rememberLazyListState()
    val topAppBarState = rememberTopAppBarState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)

    var showCategorySection by remember { mutableStateOf(true) }
    var lastVisibleItemIndex by remember { mutableIntStateOf(0) }
    val currentVisibleItemIndex by remember { derivedStateOf { productLazyListState.firstVisibleItemIndex } }
    var showAllCategories by remember { mutableStateOf(false) }

    // Track scroll direction
    LaunchedEffect(currentVisibleItemIndex) {
        // scroll down
        if (currentVisibleItemIndex > lastVisibleItemIndex) {
            showCategorySection = false
        }
        // scroll up
        if (currentVisibleItemIndex < lastVisibleItemIndex || currentVisibleItemIndex == 0) {
            showCategorySection = true
            showAllCategories = false
        }
        lastVisibleItemIndex = currentVisibleItemIndex
    }

    val mockCategory = listOf(
        TopCatalog(
            link = "",
            order = 0,
            tag = "",
            title = "New",
            image = null,
            status = TopCatalogStatus.ACTIVE,
        ),
        TopCatalog(
            link = "",
            order = 0,
            tag = "",
            title = "New",
            image = null,
            status = TopCatalogStatus.ACTIVE,
        ),
        TopCatalog(
            link = "",
            order = 0,
            tag = "",
            title = "New",
            image = null,
            status = TopCatalogStatus.ACTIVE,
        ),
        TopCatalog(
            link = "",
            order = 0,
            tag = "",
            title = "New",
            image = null,
            status = TopCatalogStatus.ACTIVE,
        ),
        TopCatalog(
            link = "",
            order = 0,
            tag = "",
            title = "New",
            image = null,
            status = TopCatalogStatus.ACTIVE,
        ),
        TopCatalog(
            link = "",
            order = 0,
            tag = "",
            title = "New",
            image = null,
            status = TopCatalogStatus.ACTIVE,
        ),
        TopCatalog(
            link = "",
            order = 0,
            tag = "",
            title = "New",
            image = null,
            status = TopCatalogStatus.ACTIVE,
        )
    )

    Scaffold(
        modifier = Modifier
            .statusBarsPadding(),
        topBar = {
            TopAppBar(
                modifier = Modifier
                    .fillMaxWidth(),
                scrollBehavior = scrollBehavior,
                windowInsets = WindowInsets(MaterialTheme.kyobiTheme.width.dp0),
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.kyobiTheme.colors.background,
                    titleContentColor = MaterialTheme.kyobiTheme.colors.background,
                    scrolledContainerColor = MaterialTheme.kyobiTheme.colors.background,
                ),
                title = {
                    CatalogSectionHeader(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(MaterialTheme.kyobiTheme.height.dp88)
                            .background(MaterialTheme.kyobiTheme.colors.background)
                            .padding(
                                start = MaterialTheme.kyobiTheme.spacing.dp0,
                                end = MaterialTheme.kyobiTheme.spacing.dp14,
                                top = MaterialTheme.kyobiTheme.spacing.dp8,
                                bottom = MaterialTheme.kyobiTheme.spacing.dp8
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(
                top = MaterialTheme.kyobiTheme.spacing.dp1,
                bottom = bottomPadding
            )
        ) {
            item {
                AnimatedVisibility(
                    visible = showCategorySection,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    CatalogSectionCategory(
                        categories = mockCategory,
                        imageLoader = imageLoader,
                        expanded = showAllCategories,
                        onAllClick = { showAllCategories = true },
                        onCollapseClick = { showAllCategories = false }
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillParentMaxHeight() // important
                ) {
                    CatalogSectionSubCategory(
                        modifier = Modifier
                            .fillMaxWidth(0.25f)
                            .fillMaxHeight(),
                        lazyListState = subCategoryLazyListState
                    )
                    CatalogSectionProductsGridView(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        lazyListState = productLazyListState
                    )
                }
            }
        }
    }
}