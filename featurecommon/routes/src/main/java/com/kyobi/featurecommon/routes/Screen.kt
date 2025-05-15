package com.kyobi.featurecommon.routes

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.navArgument
import com.kyobi.core.utils.CoreUtils.decodeBase64
import com.kyobi.core.utils.CoreUtils.encodeBase64

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

    data object HomeTab : Screen(
        routeScheme = "home",
        arguments = emptyList(),
    )

    data object CollectionTab : Screen(
        routeScheme = "collection",
        arguments = emptyList(),
    )

    // EditorVideoScreen
    data object EditorVideo : Screen(
        routeScheme = "editor-video?selectType={selectType}&uri={uri}&userId={userId}",
        arguments = listOf(
            navArgument("selectType") {
                nullable = false
                type = androidx.navigation.NavType.StringType
            },
            navArgument("uri") {
                nullable = true
                defaultValue = null
                type = androidx.navigation.NavType.StringType
            },
            navArgument("userId") {
                nullable = true
                defaultValue = null
                type = androidx.navigation.NavType.StringType
            },
        ),
    )

    data object TrendTab : Screen(
        routeScheme = "trend",
        arguments = emptyList(),
    )

    data object ProfileTab : Screen(
        routeScheme = "profile",
        arguments = emptyList(),
    )

    companion object {
        private const val BASE_URL = "http://192.168.148.2:3000"
        const val BASE_64_URL_PREFIX = "data:text/plain;base64,"
    }
}

fun NavBackStackEntry.getDecodedUserId(): String? {
    return this.arguments?.getString("userId")?.decodeBase64(Screen.BASE_64_URL_PREFIX)
}

fun NavBackStackEntry.getDecodedByKey(key: String): String? {
    return this.arguments?.getString(key)?.decodeBase64(Screen.BASE_64_URL_PREFIX)
}

@Composable
fun <T> NavHostController.getParcelable(key: String): T? = previousBackStackEntry
    ?.savedStateHandle
    ?.get<T>(key)