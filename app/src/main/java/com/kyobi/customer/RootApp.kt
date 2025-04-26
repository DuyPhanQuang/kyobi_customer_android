package com.kyobi.customer

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kyobi.createreel.CreateReelTab
import com.kyobi.customer.bottom_bar.BottomNavigationBar
import com.kyobi.customer.ui.RootUpdateVersionDialog
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.home.HomeTab
import com.kyobi.profile.ProfileTab
import com.kyobi.theme.AppTheme
import com.kyobi.trend.TrendTab
import timber.log.Timber

// Tạo CompositionLocal để cung cấp AuthViewModel
val LocalAuthViewModel = compositionLocalOf<AuthViewModel> { error("No AuthViewModel provided") }

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun RootApp(
    navController: NavHostController = rememberNavController(),
    authViewModel: AuthViewModel = hiltViewModel()
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
                composable("create-reel") {
                    CreateReelTab(navController = navController)
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