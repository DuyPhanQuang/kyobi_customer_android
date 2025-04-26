package com.kyobi.customer

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.kyobi.createreel.CreateReelTab
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
import com.kyobi.core.utils.ImageUtils.decodeBase64
import com.kyobi.createreel.editor_video.EditorVideoScreen
import com.kyobi.createreel.editor_video.EditorVideoViewModel
import com.kyobi.featurecommon.routes.Screen

// Tạo CompositionLocal để cung cấp AuthViewModel
val LocalAuthViewModel = compositionLocalOf<AuthViewModel> { error("No AuthViewModel provided") }

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RootApp(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    editorVideoViewModel: EditorVideoViewModel = hiltViewModel()
) {
    val tag = "RootApp"
    val context = LocalContext.current

    AppTheme {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                )
            },
        ) { innerPadding ->
            Timber.tag("MainScreen").d("Inner padding: top=${innerPadding.calculateTopPadding()}, bottom=${innerPadding.calculateBottomPadding()}")

            // show popup update version dialog
            RootUpdateVersionDialog()

            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize().padding(
                        bottom = innerPadding.calculateBottomPadding())
            ) {
                composable("home") {
                    HomeTab()
                }
                composable(screen = Screen.VideoUi) {
                    val sceneUri = it.getSceneUri(defaultScene = "video", context = context)
                    Timber.tag(tag).d("Scene URI for CreateReelTab: $sceneUri")
                    EditorTheme {
                        CreateReelTab(
                            navController = navController,
                            sceneUri = sceneUri,
                        )
                    }
                }
                // Thêm route cho EditorVideoScreen
                composable(screen = Screen.EditorVideo) { backStackEntry ->
                    val sceneUri = backStackEntry.arguments?.getString("sceneUri")?.decodeBase64(Screen.BASE_64_URL_PREFIX)?.toUri()
                        ?: throw IllegalArgumentException("sceneUri is required")
                    val videoUri = backStackEntry.arguments?.getString("videoUri")?.decodeBase64(Screen.BASE_64_URL_PREFIX)?.toUri()
                        ?: throw IllegalArgumentException("videoUri is required")
                    val userId = backStackEntry.arguments?.getString("userId")?.decodeBase64(Screen.BASE_64_URL_PREFIX)

                    val isExporting by editorVideoViewModel.isExporting.collectAsStateWithLifecycle()
                    val exportProgress by editorVideoViewModel.exportProgress.collectAsStateWithLifecycle()
                    val animatedProgress by animateFloatAsState(
                        targetValue = exportProgress,
                        animationSpec = tween(durationMillis = 200),
                        label = "ExportProgressAnimation"
                    )

                    EditorTheme {
                        EditorVideoScreen(
                            sceneUri = sceneUri,
                            videoUri = videoUri,
                            userId = userId,
                            editorVideoViewModel = editorVideoViewModel,
                            activity = context as Activity,
                            isExporting = isExporting,
                            animatedProgress = animatedProgress,
                            onClose = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
                composable("trend") {
                    TrendTab(navController = navController)
                }
                composable("profile") {
                    ProfileTab(navController = navController)
                }
            }
        }
    }
}

private fun NavBackStackEntry.getSceneUri(defaultScene: String, context: android.content.Context): Uri {
    val arg = arguments?.getString("scene")?.decodeBase64(ifPrefixed = Screen.BASE_64_URL_PREFIX)
    return when {
        arg == null -> "file:///android_asset/scenes/$defaultScene.scene".toUri()
        arg.startsWith("https") -> arg.toUri()
        arg.startsWith("content") -> arg.toUri()
        else -> {
            val scene = arg.takeIf {
                context.assets.list("scenes")?.contains("$it.scene") == true
            } ?: defaultScene
            "file:///android_asset/scenes/$scene.scene".toUri()
        }
    }
}

fun NavGraphBuilder.composable(
    screen: Screen,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = screen.routeScheme,
        deepLinks = listOf(navDeepLink { uriPattern = screen.deeplinkScheme }),
        arguments = screen.arguments,
        content = content,
    )
}