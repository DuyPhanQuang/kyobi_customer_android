package com.kyobi.home.ui.tab.recommended_products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.kyobi.composable.skeleton.SkeletonProductGridView
import com.kyobi.composable.space.XsSpaceY
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.featurecommon.product.ProductCard
import com.kyobi.featurecommon.product.ProductUiState
import com.kyobi.home.HomeRecommendedProductListViewModel
import com.kyobi.theme.kyobiTheme
import com.kyobi.theme.paragraphMd

@Composable
fun HomeSectionRecommendedProductsGridView(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    productListViewModel: HomeRecommendedProductListViewModel = hiltViewModel()
) {
    val productsResult by productListViewModel.products.collectAsStateWithLifecycle()
    val itemStates by productListViewModel.itemStates.collectAsStateWithLifecycle()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                start = MaterialTheme.kyobiTheme.spacing.dp12,
                end = MaterialTheme.kyobiTheme.spacing.dp12,
                top = MaterialTheme.kyobiTheme.spacing.dp16)
    ) {
        XsSpaceY()
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = "For You",
            style = MaterialTheme.kyobiTheme.typography.paragraphMd,
            color = MaterialTheme.kyobiTheme.colors.onBackground
        )
        XsSpaceY()

        when (productsResult) {
            is DomainNetworkResult.Loading -> {
                SkeletonProductGridView(modifier = Modifier.fillMaxWidth())
            }
            is DomainNetworkResult.Success -> {
                val products = (productsResult as DomainNetworkResult.Success<List<ProductUiState>>).data
                if (products.isNotEmpty()) {
                    val itemsPerRow = 2
                    val rows = products.chunked(itemsPerRow)
                    rows.forEach { rowItems ->
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.kyobiTheme.spacing.dp8)
                        ) {
                            rowItems.forEach { product ->
                                ProductCard(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = MaterialTheme.kyobiTheme.spacing.dp16),
                                    productUiState = itemStates[product.id] ?: product,
                                    imageLoader = imageLoader,
                                    onClick = {}
                                )
                            }
                            repeat(itemsPerRow - rowItems.size) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(bottom = MaterialTheme.kyobiTheme.spacing.dp16)
                                ) {}
                            }
                        }
                    }
                }
            }
            is DomainNetworkResult.Error -> {
                Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
            }
        }
    }
}