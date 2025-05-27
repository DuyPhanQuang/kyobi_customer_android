package com.kyobi.featurecommon.routes

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.kyobi.core.extensions.toNullIfStringNull
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
    data object HomeTab: Routes(
        routeScheme = RouteConstant.HOME_TAB,
        arguments = emptyList(),
    )

    // bottom tab
    data object CollectionTab: Routes(
        routeScheme = RouteConstant.COLLECTION_TAB,
        arguments = emptyList(),
    )

    // bottom tab
    data object TrendTab: Routes(
        routeScheme = RouteConstant.TREND_TAB,
        arguments = emptyList(),
    )

    // bottom tab
    data object ProfileTab: Routes(
        routeScheme = RouteConstant.PROFILE_TAB,
        arguments = emptyList(),
    )

    data object Collection: Routes(
        routeScheme = "${RouteConstant.COLLECTION}?" +
                "${RouteKey.Collection.CATEGORY_ID}={${RouteKey.Collection.CATEGORY_ID}}&" +
                "${RouteKey.Collection.SUB_CATEGORY_ID}={${RouteKey.Collection.SUB_CATEGORY_ID}}",
        arguments = listOf(
            navArgument(RouteKey.Collection.CATEGORY_ID) {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
            navArgument(RouteKey.Collection.SUB_CATEGORY_ID) {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
        ),
    )

    data object Product: Routes(
        routeScheme = "${RouteConstant.PRODUCT}?" +
                "${RouteKey.Product.ID}={${RouteKey.Product.ID}}&" +
                "${RouteKey.Product.SKU}={${RouteKey.Product.SKU}}&" +
                "${RouteKey.Product.COLOR_OPTION}={${RouteKey.Product.COLOR_OPTION}}&" +
                "${RouteKey.Product.SIZE_OPTION}={${RouteKey.Product.SIZE_OPTION}}",
        arguments = listOf(
            navArgument(RouteKey.Product.ID) {
                nullable = false
                type = NavType.StringType
            },
            navArgument(RouteKey.Product.SKU) {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
            navArgument(RouteKey.Product.COLOR_OPTION) {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
            navArgument(RouteKey.Product.SIZE_OPTION) {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
        )
    )

    data object EditorVideo: Routes(
        routeScheme = "${RouteConstant.EDITOR_VIDEO}?" +
                "${RouteKey.EditorVideo.SELECT_TYPE}={${RouteKey.EditorVideo.SELECT_TYPE}}&" +
                "${RouteKey.EditorVideo.URI}={${RouteKey.EditorVideo.URI}}&" +
                "${RouteKey.EditorVideo.USER_ID}={${RouteKey.EditorVideo.USER_ID}}",
        arguments = listOf(
            navArgument(RouteKey.EditorVideo.SELECT_TYPE) {
                nullable = false
                type = NavType.StringType
            },
            navArgument(RouteKey.EditorVideo.URI) {
                nullable = true
                defaultValue = null
                type = NavType.StringType
            },
            navArgument(RouteKey.EditorVideo.USER_ID) {
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
    return this.arguments?.getString(key)?.decodeBase64(Routes.BASE_64_URL_PREFIX)?.toNullIfStringNull()
}

@Composable
fun <T> NavHostController.getParcelable(key: String): T? = previousBackStackEntry?.savedStateHandle?.get<T>(key)