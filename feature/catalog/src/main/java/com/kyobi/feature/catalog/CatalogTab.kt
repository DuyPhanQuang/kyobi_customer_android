package com.kyobi.feature.catalog

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kyobi.featurecommon.auth.AuthViewModel

@Composable
fun CatalogTab(
    navController: NavController,
    viewModel: CatalogTabViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    topPadding: Dp,
    bottomPadding: Dp,
) {

}