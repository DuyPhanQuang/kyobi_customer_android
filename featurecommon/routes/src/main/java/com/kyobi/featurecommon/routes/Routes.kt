package com.kyobi.featurecommon.routes

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.kyobi.core.utils.CoreUtils.decodeBase64
import com.kyobi.core.utils.CoreUtils.encodeBase64

sealed class Routes(
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

    // bottom tab
    data object HomeTab : Routes(
        routeScheme = "home-tab",
        arguments = emptyList(),
    )

    // bottom tab
    data object CollectionTab : Routes(
        routeScheme = "collection-tab",
        arguments = emptyList(),
    )

    // bottom tab
    data object TrendTab : Routes(
        routeScheme = "trend-tab",
        arguments = emptyList(),
    )

    // bottom tab
    data object ProfileTab : Routes(
        routeScheme = "profile-tab",
        arguments = emptyList(),
    )

    data object Collection : Routes(
        routeScheme = "collection?categoryId={categoryId}",
        arguments = listOf(
            navArgument("categoryId") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
        ),
    )

    data object EditorVideo : Routes(
        routeScheme = "editor-video?selectType={selectType}&uri={uri}&userId={userId}",
        arguments = listOf(
            navArgument("selectType") {
                nullable = false
                type = NavType.StringType
            },
            navArgument("uri") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
            navArgument("userId") {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
        ),
    )

    companion object {
        private const val BASE_URL = "http://192.168.148.2:3000" //deeplink url website
        const val BASE_64_URL_PREFIX = "data:text/plain;base64,"
    }
}

fun NavBackStackEntry.getDecodedUserId(): String? {
    return this.arguments?.getString("userId")?.decodeBase64(Routes.BASE_64_URL_PREFIX)
}

fun NavBackStackEntry.getDecodedByKey(key: String): String? {
    return this.arguments?.getString(key)?.decodeBase64(Routes.BASE_64_URL_PREFIX)
}

@Composable
fun <T> NavHostController.getParcelable(key: String): T? = previousBackStackEntry
    ?.savedStateHandle
    ?.get<T>(key)