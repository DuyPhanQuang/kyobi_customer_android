package com.kyobi.customer

import android.net.Uri
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
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
import com.kyobi.feature.collection.CollectionTab
import com.kyobi.featurecommon.routes.Screen
import com.kyobi.featurecommon.routes.getDecodedByKey
import com.kyobi.featurecommon.routes.getDecodedUserId
import com.kyobi.featurecommon.routes.getParcelable
import ly.img.camera.core.CameraResult

// Tạo CompositionLocal để cung cấp AuthViewModel
val LocalAuthViewModel = compositionLocalOf<AuthViewModel> { error("No AuthViewModel provided") }

@Composable
fun RootApp(
    navController: NavHostController,
    deepLinkState: State<Uri?>,
    authViewModel: AuthViewModel = hiltViewModel(),
    editorVideoViewModel: EditorVideoViewModel = hiltViewModel()
) {
    val tag = "RootApp"
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute != Screen.EditorVideo.routeScheme

    // Xử lý deep link
    LaunchedEffect(deepLinkState.value) {
        val deepLinkUri = deepLinkState.value
        if (deepLinkUri != null) {
            Timber.tag(tag).d("Handling deep link with URI: $deepLinkUri")
            try {
                when (deepLinkUri.path) {
                    "/" -> {
                        navController.navigate(Screen.HomeTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    "/home" -> {
                        navController.navigate(Screen.HomeTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    "/collection" -> {
                        navController.navigate(Screen.CollectionTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    "/trend" -> {
                        navController.navigate(Screen.TrendTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    "/profile" -> {
                        navController.navigate(Screen.ProfileTab.routeScheme) {
                            popUpTo(navController.graph.startDestinationId) {
                                inclusive = true
                            }
                            launchSingleTop = true
                        }
                    }
                    else -> {
                        Timber.tag(tag).w("Unknown deeplink path: ${deepLinkUri.path}, falling back to Home")
                        navController.navigate(Screen.HomeTab.routeScheme) {
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
                startDestination = "home",
            ) {
                composable(screen = Screen.HomeTab) {
                    HomeTab(
                        navController = navController,
                        topPadding = innerPadding.calculateTopPadding(),
                        bottomPadding = innerPadding.calculateBottomPadding()
                    )
                }
                composable(screen = Screen.CollectionTab) {
                    CollectionTab(
                        navController = navController,
                        topPadding = innerPadding.calculateTopPadding(),
                        bottomPadding = innerPadding.calculateBottomPadding()
                    )
                }
                composable(screen = Screen.TrendTab) {
                    TrendTab(
                        navController = navController,
                        topPadding = innerPadding.calculateTopPadding(),
                        bottomPadding = innerPadding.calculateBottomPadding()
                    )
                }
                composable(screen = Screen.ProfileTab) {
                    ProfileTab(navController = navController)
                }
                // EditorVideoScreen
                composable(screen = Screen.EditorVideo) {
                    val selectTypeString = it.getDecodedByKey("selectType")
                    val selectType = enumValueOf<SelectMediaType>(selectTypeString!!)
                    val uri = it.getDecodedByKey("uri")?.toUri()
                    val recording = navController.getParcelable<CameraResult.Record>("recording")

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