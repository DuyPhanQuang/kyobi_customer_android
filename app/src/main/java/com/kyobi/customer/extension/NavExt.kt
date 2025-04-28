package com.kyobi.customer.extension

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.runtime.Composable
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import com.kyobi.featurecommon.routes.Screen

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