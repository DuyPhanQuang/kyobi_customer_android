package com.kyobi.featurecommon.product.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.kyobi.composable.space.XsSpaceY
import com.kyobi.featurecommon.auth.AuthViewModel
import com.kyobi.featurecommon.product.ProductDetailViewModel

@Composable
fun ProductDetailScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    productId: String,
    topPadding: Dp,
    bottomPadding: Dp
) {
    val viewModel: ProductDetailViewModel = hiltViewModel()

    Box() {
        XsSpaceY()
    }
}