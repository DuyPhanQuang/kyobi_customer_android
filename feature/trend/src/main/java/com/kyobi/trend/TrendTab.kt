package com.kyobi.trend

import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.trend.ui.ReelList
import com.kyobi.trend.ui.ReelPlaybackViewModel

@OptIn(UnstableApi::class)
@Composable
fun TrendTab(
    navController: NavController,
    viewModel: TrendTabViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    reelPlaybackViewModel: ReelPlaybackViewModel = hiltViewModel(),
    topPadding: Dp,
    bottomPadding: Dp,
) {
    val authUiState by authViewModel.authUiState.collectAsStateWithLifecycle()
    val uiState = viewModel.trendTabUiState
    val recyclerViewRef = remember { mutableStateOf<RecyclerView?>(null) }
    val imageLoader = viewModel.getImageLoader()

    ReelList(
        topSystemBarHeight = topPadding,
        bottomNavBarHeight = bottomPadding,
        viewModel = reelPlaybackViewModel,
        imageLoader = imageLoader
    )

}