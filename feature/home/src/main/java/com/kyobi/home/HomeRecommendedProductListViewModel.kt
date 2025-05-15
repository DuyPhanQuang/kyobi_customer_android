package com.kyobi.home

import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.usecase.AddRemoveProductToFavoriteUseCase
import com.kyobi.domain.usecase.AddToCartUseCase
import com.kyobi.domain.usecase.GetProductRecommendationsUseCase
import com.kyobi.domain.usecase.GetProductsUseCase
import com.kyobi.featurecommon.product.BaseProductListViewModel
import com.kyobi.featurecommon.product.ProductUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeRecommendedProductListViewModel @Inject constructor(
    private val getProductRecommendationsUseCase: GetProductRecommendationsUseCase,
    private val getProductsUseCase: GetProductsUseCase,
    addToCartUseCase: AddToCartUseCase,
    addRemoveProductToFavoriteUseCase: AddRemoveProductToFavoriteUseCase,
): BaseProductListViewModel(
    addToCartUseCase,
    addRemoveProductToFavoriteUseCase
) {
    private val tag = "HomeRecommendedProductListViewModel"

    init {
        fetchProductRecommendations(emptyList(), emptyList())
    }

    private fun fetchProductRecommendations(
        cartProductIds: List<String>,
        recentlyViewedProductIds: List<String>
    ) {
        viewModelScope.launchOnIO {
            productsResult.value = DomainNetworkResult.Loading
            try {
                val allProductIds = (cartProductIds + recentlyViewedProductIds).distinct()
                val recommendedProducts = mutableListOf<Product>()
                for (productId in allProductIds.take(10)) {
                    getProductRecommendationsUseCase.invoke(productId).collect { result ->
                        if (result is DomainNetworkResult.Success) {
                            result.data.forEach { product ->
                                if (!recommendedProducts.any { it.id == product.id }) {
                                    recommendedProducts.add(product)
                                }
                            }
                        }
                    }
                }
                if (recommendedProducts.isEmpty()) {
                    getProductsUseCase.invoke(
                        query = "tag:women",
                        reverse = null,
                        sortKey = null,
                        identifiers = null,
                        first = null
                    ).collect { result ->
                        when (result) {
                            is DomainNetworkResult.Success -> {
                                productsResult.value = DomainNetworkResult.Success(
                                    result.data.map { ProductUiState.fromProduct(it) }
                                )
                            }
                            is DomainNetworkResult.Loading -> {
                                productsResult.value = DomainNetworkResult.Loading
                            }
                            is DomainNetworkResult.Error -> {
                                productsResult.value = result
                            }
                        }
                    }
                } else {
                    productsResult.value = DomainNetworkResult.Success(
                        recommendedProducts.map { ProductUiState.fromProduct(it) }
                    )
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to fetch product recommendations")
                productsResult.value = DomainNetworkResult.Error.Generic(e)
            }
        }
    }
}