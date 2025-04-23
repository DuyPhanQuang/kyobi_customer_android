package com.kyobi.customer

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kyobi.customer.bottom_bar.BottomNavigationBar
import com.kyobi.customer.ui.RootDialogContainer
import com.kyobi.home.HomeTab
import com.kyobi.profile.ProfileTab
import com.kyobi.theme.AppTheme
import com.kyobi.trend.TrendTab
import timber.log.Timber

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RootApp(
    navController: NavHostController = rememberNavController()
) {

    AppTheme {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navController = navController,
                )
            },
        ) { innerPadding ->
            Timber.tag("MainScreen").d("Inner padding: top=${innerPadding.calculateTopPadding()}, bottom=${innerPadding.calculateBottomPadding()}")

            RootDialogContainer()

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