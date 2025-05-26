package com.kyobi.feature.collection.ui.tab.products

import androidx.compose.foundation.background
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
import com.kyobi.domain.model.Product
import com.kyobi.feature.collection.screen.tab.CollectionTabProductListViewModel
import com.kyobi.feature.collection.ui.tab.sort_filter.CollectionTabSectionSortFilter
import com.kyobi.featurecommon.product.ui.ProductCard
import com.kyobi.featurecommon.product.ProductUiState
import com.kyobi.theme.kyobiTheme

@Composable
fun CollectionTabSectionProductsGridView(
    modifier: Modifier = Modifier,
    imageLoader: ImageLoader,
    productListViewModel: CollectionTabProductListViewModel,
    lazyGridState: LazyGridState,
    bottomPadding: Dp,
    onSortClick: () -> Unit,
    onFilterClick: () -> Unit,
    onProductClick: (Product) -> Unit
) {
    val productsResult by productListViewModel.products.collectAsStateWithLifecycle()
    val itemStates by productListViewModel.itemStates.collectAsStateWithLifecycle()

    val spacing = MaterialTheme.kyobiTheme.spacing
    val colorTheme = MaterialTheme.kyobiTheme.colors

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        state = lazyGridState,
        modifier = modifier
            .background(colorTheme.background),
        contentPadding = PaddingValues(
            bottom = bottomPadding,
            start = spacing.dp12,
            end = spacing.dp12),
        horizontalArrangement = Arrangement.spacedBy(spacing.dp8)
    ) {
        stickyHeader {
            CollectionTabSectionSortFilter(
                modifier = Modifier.fillMaxWidth(),
                onSortClick = onSortClick,
                onFilterClick = onFilterClick
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
                val productsUiStates = (productsResult as DomainNetworkResult.Success<List<ProductUiState>>).data
                items(
                    productsUiStates,
                    key = { "product_${it.id}" }
                ) { productUiState ->
                    ProductCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = spacing.dp12),
                        productUiState = itemStates[productUiState.id] ?: productUiState,
                        imageLoader = imageLoader,
                        onClick = { onProductClick(productUiState.product) }
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