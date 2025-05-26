package com.kyobi.feature.collection.ui.collection.products

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.kyobi.composable.skeleton.SkeletonProductCard
import com.kyobi.composable.space.SpaceY
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.feature.collection.screen.collection.CollectionScreenProductListViewModel
import com.kyobi.featurecommon.product.ProductUiState
import com.kyobi.featurecommon.product.ui.ProductCard
import com.kyobi.theme.kyobiTheme

@Composable
fun CollectionSectionProductsGridView(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    productListViewModel: CollectionScreenProductListViewModel,
    lazyGridState: LazyGridState,
    bottomPadding: Dp,
) {
    val productsResult by productListViewModel.products.collectAsStateWithLifecycle()
    val itemStates by productListViewModel.itemStates.collectAsStateWithLifecycle()

    val spacing = MaterialTheme.kyobiTheme.spacing

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        state = lazyGridState,
        contentPadding = PaddingValues(
            bottom = bottomPadding,
            start = spacing.dp12,
            end = spacing.dp12
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing.dp8)
    ) {
        when (productsResult) {
            is DomainNetworkResult.Loading -> {
                items(4) {
                    SkeletonProductCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.dp12)
                    )
                }
            }
            is DomainNetworkResult.Success -> {
                val productUiStates = (productsResult as DomainNetworkResult.Success<List<ProductUiState>>).data
                items(
                    productUiStates,
                    key = { "product_${it.id}" }
                ) { productUiState ->
                    ProductCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.dp12),
                        productUiState = itemStates[productUiState.id] ?: productUiState,
                        imageLoader = imageLoader,
                        onClick = {}
                    )
                }
            }
            is DomainNetworkResult.Error -> {
                item {
                    spacing.dp0.SpaceY()
                }
            }
        }
    }
}