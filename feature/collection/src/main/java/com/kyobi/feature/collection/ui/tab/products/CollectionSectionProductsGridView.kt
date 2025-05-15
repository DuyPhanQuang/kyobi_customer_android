package com.kyobi.feature.collection.ui.tab.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.ImageLoader
import com.kyobi.composable.skeleton.SkeletonProductCard
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.feature.collection.CollectionTabProductListViewModel
import com.kyobi.feature.collection.ui.tab.sort_filter.CollectionSectionSortFilter
import com.kyobi.featurecommon.product.ProductCard
import com.kyobi.featurecommon.product.ProductUiState
import com.kyobi.theme.kyobiTheme

@Composable
fun CollectionSectionProductsGridView(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    productListViewModel: CollectionTabProductListViewModel = hiltViewModel(),
    lazyListState: LazyGridState,
    bottomPadding: Dp,
) {
    val productsResult by productListViewModel.products.collectAsStateWithLifecycle()
    val itemStates by productListViewModel.itemStates.collectAsStateWithLifecycle()

    val spacing = MaterialTheme.kyobiTheme.spacing

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = lazyListState,
        modifier = modifier
            .background(MaterialTheme.kyobiTheme.colors.background),
        contentPadding = PaddingValues(
            bottom = bottomPadding,
            start = spacing.dp12,
            end = spacing.dp12
        ),
        horizontalArrangement = Arrangement.spacedBy(spacing.dp8)
    ) {
        stickyHeader {
            CollectionSectionSortFilter(
                modifier = Modifier.fillMaxWidth()
            )
        }
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
                val products = (productsResult as DomainNetworkResult.Success<List<ProductUiState>>).data
                items(products) { product ->
                    ProductCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.dp12),
                        productUiState = itemStates[product.id] ?: product,
                        imageLoader = imageLoader,
                        onClick = {}
                    )
                }
            }
            is DomainNetworkResult.Error -> {
                item {
                    Spacer(modifier = Modifier.height(MaterialTheme.kyobiTheme.height.dp0))
                }
            }
        }
    }
}