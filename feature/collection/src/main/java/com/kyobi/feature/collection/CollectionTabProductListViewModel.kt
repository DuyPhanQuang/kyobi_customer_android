package com.kyobi.feature.collection

import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.core.extensions.toQueryBySingleTag
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.usecase.AddRemoveProductToFavoriteUseCase
import com.kyobi.domain.usecase.AddToCartUseCase
import com.kyobi.domain.usecase.GetProductsUseCase
import com.kyobi.featurecommon.product.BaseProductListViewModel
import com.kyobi.featurecommon.product.ProductUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionTabProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    addToCartUseCase: AddToCartUseCase,
    addRemoveProductToFavoriteUseCase: AddRemoveProductToFavoriteUseCase,
): BaseProductListViewModel(
    addToCartUseCase,
    addRemoveProductToFavoriteUseCase
) {
    private val tag = "CollectionTabProductListViewModel"

    init {
        fetchAllProducts()
    }

    private fun fetchAllProducts() {
        viewModelScope.launchOnIO {
            productsResult.value = DomainNetworkResult.Loading
            try {
                val tag = "women"
                getProductsUseCase.invoke(
                    query = tag.toQueryBySingleTag(),
                    reverse = null,
                    sortKey = null,
                    identifiers = null,
                    first = 250
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
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to fetch all products")
            }
        }
    }
}