package com.kyobi.featurecommon.routes

import androidx.navigation.NamedNavArgument
import androidx.navigation.navArgument
import com.kyobi.core.utils.ImageUtils.encodeBase64

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

    // EditorVideoScreen
    data object EditorVideo : Screen(
        routeScheme = "editor-video?sceneUri={sceneUri}&videoUri={videoUri}&userId={userId}",
        arguments = listOf(
            navArgument("sceneUri") {
                nullable = false
                type = androidx.navigation.NavType.StringType
            },
            navArgument("videoUri") {
                nullable = false
                type = androidx.navigation.NavType.StringType
            },
            navArgument("userId") {
                nullable = true
                defaultValue = null
                type = androidx.navigation.NavType.StringType
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