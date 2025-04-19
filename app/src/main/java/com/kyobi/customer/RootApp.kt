package com.kyobi.customer

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kyobi.customer.bottom_bar.BottomNavigationBar
import com.kyobi.home.HomeTab
import com.kyobi.profile.ProfileTab
import com.kyobi.theme.AppTheme

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
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable("home") {
                    HomeTab()
                }
                composable("profile") {
                    ProfileTab(navController = navController)
                }
            }
        }
    }
}