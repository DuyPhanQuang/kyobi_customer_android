package com.kyobi.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.material3.*
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.kyobi.home.ui.tab.HomeSectionBanner
import com.kyobi.home.ui.tab.HomeSectionHeader
import com.kyobi.theme.kyobiTheme

@Composable
fun HomeTab(
    navController: NavHostController,
    viewModel: HomeTabViewModel = hiltViewModel(),
    topPadding: Dp,
    bottomPadding: Dp,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val lazyListState = rememberLazyListState()
    val maxScrollOffset = 320f // total height of section header + section banner
    val scrollOffset = remember { derivedStateOf { lazyListState.firstVisibleItemScrollOffset } }
    val scrollAlphaAnimation by animateFloatAsState(
        targetValue = (scrollOffset.value.toFloat() / maxScrollOffset).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 300),
        label = "ScrollAlphaAnimation"
    )

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(
                top = MaterialTheme.kyobiTheme.spacing.dp0,
                bottom = bottomPadding
            ),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .zIndex(0f)
                    .fillMaxSize()
            ) {
                item {
                    HomeSectionBanner()
                }
                item {
                    Box(
                        modifier = Modifier
                            .background(Color.Red)
                            .fillMaxWidth()
                            .height(1000.dp)
                    )
                }
            }
            // absolute - pinned top
            Box(
                modifier = Modifier.zIndex(1f)
            ) {
                HomeSectionHeader(
                    topPadding = topPadding,
                    scrollAlpha = scrollAlphaAnimation,
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
}