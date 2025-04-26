package com.kyobi.customer

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
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
import java.nio.charset.Charset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import androidx.core.net.toUri
import com.kyobi.domain.usecase.AssetSourceUsecase
import com.kyobi.domain.usecase.AssetUsecase

// Tạo CompositionLocal để cung cấp AuthViewModel
val LocalAuthViewModel = compositionLocalOf<AuthViewModel> { error("No AuthViewModel provided") }

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RootApp(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel(),
    assetSourceUsecase: AssetSourceUsecase,
    assetUsecase: AssetUsecase,
) {
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
                    EditorTheme {
                        CreateReelTab(
                            navController = navController,
                            sceneUri = sceneUri,
                            assetSourceUsecase = assetSourceUsecase,
                            assetUsecase = assetUsecase,
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

sealed class Screen(
    val routeScheme: String,
    val arguments: List<NamedNavArgument>,
) {
    val deeplinkScheme = "$BASE_URL/$routeScheme"

    fun getRoute(vararg args: Pair<String, Any?>): String = args.fold(routeScheme) { acc, arg ->
        val (key, value) = arg
        acc.replace(
            "{$key}",
            value
                .toString()
                .encodeBase64(withPrefix = BASE_64_URL_PREFIX),
        )
    }

    data object Home : Screen(
        routeScheme = "home",
        arguments = emptyList(),
    )

    data object VideoUi : Screen(
        routeScheme = "create-reel?scene={scene}",
        arguments = listOf(
            navArgument("scene") {
                nullable = true
                defaultValue = null
            },
        ),
    )

    data object Trend : Screen(
        routeScheme = "trend",
        arguments = emptyList(),
    )

    data object Profile : Screen(
        routeScheme = "profile",
        arguments = emptyList(),
    )

    companion object {
        private const val BASE_URL = "https://ubq.page.link"
        const val BASE_64_URL_PREFIX = "data:text/plain;base64,"
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

@OptIn(ExperimentalEncodingApi::class)
private fun String.encodeBase64(withPrefix: String = ""): String = withPrefix + Base64.encode(this.toByteArray(
    Charset.forName("UTF-8")))

@OptIn(ExperimentalEncodingApi::class)
private fun String.decodeBase64(ifPrefixed: String = ""): String = if (ifPrefixed.isEmpty() || this.startsWith(ifPrefixed)) {
    Base64.decode(this.removePrefix(ifPrefixed)).decodeToString()
} else {
    this
}

@Composable
private fun <T> NavHostController.getParcelable(key: String): T? = previousBackStackEntry
    ?.savedStateHandle
    ?.get<T>(key)
