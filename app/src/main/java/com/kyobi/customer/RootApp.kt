package com.kyobi.customer

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.kyobi.home.HomeTab

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun RootApp() {
    MaterialTheme {
        HomeTab()
    }
}