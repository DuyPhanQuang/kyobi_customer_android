package com.kyobi.customer

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
    navController: NavHostController,
    authViewModel: AuthViewModel = hiltViewModel(),
    editorVideoViewModel: EditorVideoViewModel = hiltViewModel()
) {
    val tag = "RootApp"
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val shouldShowBottomBar = currentRoute != Screen.EditorVideo.routeScheme

    AppTheme {
        Scaffold(
            bottomBar = {
                if (shouldShowBottomBar) {
                    BottomNavigationBar(navController = navController)
                }
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