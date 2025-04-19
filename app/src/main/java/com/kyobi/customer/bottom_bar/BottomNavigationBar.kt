package com.kyobi.customer.bottom_bar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.kyobi.customer.R
import com.kyobi.theme.kyobiTheme

data class BottomNavItem(
    val route: String,
    val iconResId: Int,
    val label: String,
    val badgeCount: Int? = null
)

val bottomNavItems = listOf(
    BottomNavItem(
        route = "home",
        iconResId = R.drawable.ic_home_tab,
        label = "Home"
    ),
    BottomNavItem(
        route = "profile",
        iconResId = R.drawable.ic_profile_tab,
        label = "Profile",
        badgeCount = 3
    )
)

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(
        modifier = modifier
            .background(MaterialTheme.kyobiTheme.colors.surface),
        containerColor = MaterialTheme.kyobiTheme.colors.surface,
        contentColor = MaterialTheme.kyobiTheme.colors.onSurface
    ) {
        bottomNavItems.forEach { item ->
            val isSelected = currentDestination?.hierarchy?.any { it.route == item.route } == true
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = {
                    AnimatedContent(
                        targetState = isSelected,
                        transitionSpec = {
                            (fadeIn(animationSpec = tween(300)) + fadeIn()).togetherWith(
                                fadeOut(
                                    animationSpec = tween(300)
                                )
                            )
                        },
                        label = "Badge ${item.label}"
                    ) { selected ->
                        BadgedBox(badge = {
                            if (item.badgeCount != null) {
                                Badge {
                                    Text(
                                        text = item.badgeCount.toString(),
                                        color = MaterialTheme.kyobiTheme.colors.text.neutral50
                                    )
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = item.iconResId),
                                contentDescription = item.label,
                                modifier = Modifier.size(
                                    if (selected) MaterialTheme.kyobiTheme.icon.lg
                                    else MaterialTheme.kyobiTheme.icon.lg
                                ),
                                tint = MaterialTheme.kyobiTheme.colors.text.neutral50
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.kyobiTheme.typography.labelSmall,
                        color = if (isSelected) MaterialTheme.kyobiTheme.colors.primary
                        else MaterialTheme.kyobiTheme.colors.secondary
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.kyobiTheme.colors.primary,
                    unselectedIconColor = MaterialTheme.kyobiTheme.colors.secondary,
                    selectedTextColor = MaterialTheme.kyobiTheme.colors.primary,
                    unselectedTextColor = MaterialTheme.kyobiTheme.colors.secondary,
                )
            )
        }
    }
}