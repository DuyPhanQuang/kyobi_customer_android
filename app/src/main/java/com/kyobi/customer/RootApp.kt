package com.kyobi.customer

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.kyobi.customer.bottom_bar.BottomNavigationBar
import com.kyobi.customer.ui.RootUpdateVersionDialog
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.home.HomeTab
import com.kyobi.profile.ProfileTab
import com.kyobi.theme.AppTheme
import com.kyobi.trend.TrendTab
import ly.img.editor.core.theme.EditorTheme
import timber.log.Timber
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kyobi.createreel.editor_video.EditorVideoScreen
import com.kyobi.createreel.editor_video.EditorVideoViewModel
import com.kyobi.createreel.editor_video.SelectMediaType
import com.kyobi.customer.extension.composable
import com.kyobi.domain.model.Product
import com.kyobi.feature.collection.screen.collection.CollectionScreen
import com.kyobi.feature.collection.screen.tab.CollectionTab
import com.kyobi.feature.collection.screen.tab.CollectionTabViewModel
import com.kyobi.featurecommon.product.screen.ProductDetailScreen
import com.kyobi.featurecommon.routes.RouteConstant
import com.kyobi.featurecommon.routes.RouteKey
import com.kyobi.featurecommon.routes.Routes
import com.kyobi.featurecommon.routes.getDecodedByKey
import com.kyobi.featurecommon.routes.getDecodedUserId
import com.kyobi.featurecommon.routes.getParcelable
import com.kyobi.home.HomeTabViewModel
import com.kyobi.trend.TrendTabViewModel
import com.kyobi.trend.ui.ReelPlaybackViewModel
import ly.img.camera.core.CameraResult

// Tạo CompositionLocal để provide AuthViewModel
val LocalAuthViewModel = compositionLocalOf<AuthViewModel> { error("No AuthViewModel provided") }

// Tạo CompositionLocal để provide HomeTabViewModel
val LocalHomeTabViewModel = compositionLocalOf<HomeTabViewModel> { error("No HomeTabViewModel provided") }

// Tạo CompositionLocal để provide CollectionTabViewModel
val LocalCollectionTabViewModel = compositionLocalOf<CollectionTabViewModel> { error("No CollectionTabViewModel provided") }

// Tạo CompositionLocal để provide TrendTabViewModel
val LocalTrendTabViewModel = compositionLocalOf<TrendTabViewModel> { error("No TrendTabViewModel provided") }

// Tạo CompositionLocal để provide ReelPlaybackViewModel
val LocalReelPlaybackViewModel = compositionLocalOf<ReelPlaybackViewModel> { error("No ReelPlaybackViewModel provided") }

@Composable
fun RootApp(
    navController: NavHostController,
    deepLinkState: State<Uri?>,
    authViewModel: AuthViewModel = hiltViewModel(),
    homeTabViewModel: HomeTabViewModel = hiltViewModel(),
    collectionTabViewModel: CollectionTabViewModel = hiltViewModel(),
    trendTabViewModel: TrendTabViewModel = hiltViewModel(),
    editorVideoViewModel: EditorVideoViewModel = hiltViewModel(),
    reelPlaybackViewModel: ReelPlaybackViewModel = hiltViewModel(),
) {
    val tag = "RootApp"
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute == Routes.HomeTab.routeScheme ||
            currentRoute == Routes.CollectionTab.routeScheme ||
            currentRoute == Routes.TrendTab.routeScheme ||
            currentRoute == Routes.ProfileTab.routeScheme

    // Xử lý deep link
    LaunchedEffect(deepLinkState.value) {
        val deepLinkUri = deepLinkState.value
        if (deepLinkUri != null) {
            Timber.tag(tag).d("Handling deep link with URI: $deepLinkUri")
            try {
                when (deepLinkUri.path) {
                    "/" -> {
                        navController.navigate(Routes.HomeTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    "/${RouteConstant.HOME_TAB}" -> {
                        navController.navigate(Routes.HomeTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    "/${RouteConstant.COLLECTION_TAB}" -> {
                        navController.navigate(Routes.CollectionTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    "/${RouteConstant.TREND_TAB}" -> {
                        navController.navigate(Routes.TrendTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    "/${RouteConstant.PROFILE_TAB}" -> {
                        navController.navigate(Routes.ProfileTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    else -> {
                        Timber.tag(tag).w("Unknown deeplink path: ${deepLinkUri.path}, falling back to Home")
                        navController.navigate(Routes.HomeTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to navigate with deep link: $deepLinkUri")
            }
        }
    }

    CompositionLocalProvider(
        LocalAuthViewModel provides authViewModel,
        LocalHomeTabViewModel provides homeTabViewModel,
        LocalCollectionTabViewModel provides collectionTabViewModel,
        LocalTrendTabViewModel provides trendTabViewModel,
        LocalReelPlaybackViewModel provides reelPlaybackViewModel
    ) {
        AppTheme {
            Scaffold(
                bottomBar = {
                    if (shouldShowBottomBar) {
                        BottomNavigationBar(navController = navController)
                    }
                },
            ) { innerPadding ->
                // show popup update version dialog
                RootUpdateVersionDialog()

                NavHost(
                    navController = navController,
                    startDestination = RouteConstant.HOME_TAB,
                ) {
                    composable(routes = Routes.HomeTab) {
                        HomeTab(
                            navController = navController,
                            authViewModel = LocalAuthViewModel.current,
                            viewModel = LocalHomeTabViewModel.current,
                            topPadding = innerPadding.calculateTopPadding(),
                            bottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }
                    composable(routes = Routes.CollectionTab) {
                        CollectionTab(
                            navController = navController,
                            authViewModel = LocalAuthViewModel.current,
                            viewModel = LocalCollectionTabViewModel.current,
                            bottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }
                    composable(routes = Routes.TrendTab) {
                        TrendTab(
                            navController = navController,
                            authViewModel = LocalAuthViewModel.current,
                            viewModel = LocalTrendTabViewModel.current,
                            reelPlaybackViewModel = LocalReelPlaybackViewModel.current,
                            topPadding = innerPadding.calculateTopPadding(),
                            bottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }
                    composable(routes = Routes.ProfileTab) {
                        ProfileTab(navController = navController)
                    }
                    composable(routes = Routes.Collection) {
                        val categoryIdFromNav = it.getDecodedByKey(RouteKey.Collection.CATEGORY_ID)
                        val subCategoryIdFromNav = it.getDecodedByKey(RouteKey.Collection.SUB_CATEGORY_ID)
                        CollectionScreen(
                            navController = navController,
                            authViewModel = LocalAuthViewModel.current,
                            collectionTabViewModel = LocalCollectionTabViewModel.current,
                            categoryId = categoryIdFromNav,
                            subCategoryId = subCategoryIdFromNav,
                            bottomPadding = innerPadding.calculateBottomPadding()
                        )
                    }
                    composable(routes = Routes.Product) {
                        val productIdFromNav = it.getDecodedByKey(RouteKey.Product.ID)!!
                        val colorOptionFromNav = it.getDecodedByKey(RouteKey.Product.COLOR_OPTION)
                        val sizeOptionFromNav = it.getDecodedByKey(RouteKey.Product.SIZE_OPTION)
                        val skuFromNav = it.getDecodedByKey(RouteKey.Product.SKU)
                        val productDataFromNav = navController.getParcelable<Product>(RouteKey.Product.DATA)
                        ProductDetailScreen(
                            navController = navController,
                            authViewModel = LocalAuthViewModel.current,
                            productId = productIdFromNav,
                            initProduct = productDataFromNav,
                            topPadding = innerPadding.calculateTopPadding(),
                            bottomPadding = innerPadding.calculateBottomPadding(),
                        )
                    }
                    composable(routes = Routes.EditorVideo) {
                        val selectTypeString = it.getDecodedByKey(RouteKey.EditorVideo.SELECT_TYPE)
                        val selectType = enumValueOf<SelectMediaType>(selectTypeString!!)
                        val uri = it.getDecodedByKey(RouteKey.EditorVideo.URI)?.toUri()
                        val recording = navController.getParcelable<CameraResult.Record>(RouteKey.EditorVideo.RECORDING)
                        val userId = it.getDecodedUserId()
                        val isExporting by editorVideoViewModel.isExporting.collectAsStateWithLifecycle()
                        val exportProgress by editorVideoViewModel.exportProgress.collectAsStateWithLifecycle()
                        val animatedProgress by animateFloatAsState(
                            targetValue = exportProgress,
                            animationSpec = tween(durationMillis = 200),
                            label = "ExportProgressAnimation"
                        )
                        EditorTheme {
                            EditorVideoScreen(
                                selectType = selectType,
                                uri = uri,
                                cameraResult = recording,
                                userId = userId,
                                editorVideoViewModel = editorVideoViewModel,
                                isExporting = isExporting,
                                animatedProgress = animatedProgress,
                                onClose = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}