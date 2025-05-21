package com.kyobi.customer.extension

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.kyobi.featurecommon.routes.Routes

fun NavGraphBuilder.composable(
    routes: Routes,
    content: @Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = routes.routeScheme,
        deepLinks = listOf(navDeepLink { uriPattern = routes.deeplinkScheme }),
        arguments = routes.arguments,
        content = content,
    )
}