package com.kyobi.customer

import android.annotation.SuppressLint
import android.app.Activity
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
import com.kyobi.createreel.editor_video.EditorVideoScreen
import com.kyobi.createreel.editor_video.EditorVideoViewModel
import com.kyobi.createreel.editor_video.SelectMediaType
import com.kyobi.featurecommon.routes.Screen
import com.kyobi.featurecommon.routes.getDecodedByKey
import com.kyobi.featurecommon.routes.getDecodedUserId
import com.kyobi.featurecommon.routes.getParcelable
import ly.img.camera.core.CameraResult

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
            Timber.tag(tag).d("Inner padding: top=${innerPadding.calculateTopPadding()}, bottom=${innerPadding.calculateBottomPadding()}")

            // show popup update version dialog
            RootUpdateVersionDialog()

            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier
                    .fillMaxSize().padding(
                        bottom = innerPadding.calculateBottomPadding())
            ) {
                composable(screen = Screen.Home) {
                    HomeTab()
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
                            activity = context as Activity,
                            isExporting = isExporting,
                            animatedProgress = animatedProgress,
                            onClose = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
                composable(screen = Screen.Trend) {
                    TrendTab(navController = navController)
                }
                composable(screen = Screen.Profile) {
                    ProfileTab(navController = navController)
                }
            }
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