package com.kyobi.featurecommon.product

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.usecase.AddRemoveProductToFavoriteUseCase
import com.kyobi.domain.usecase.AddToCartUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class BaseProductListViewModel(
    protected val addToCartUseCase: AddToCartUseCase,
    protected val addRemoveProductToFavoriteUseCase: AddRemoveProductToFavoriteUseCase
): ViewModel() {
    protected val _products = MutableStateFlow<DomainNetworkResult<List<ProductUiState>>>(DomainNetworkResult.Loading)
    val products = _products.asStateFlow()

    protected val _itemStates = MutableStateFlow<Map<String, ProductUiState>>(emptyMap())
    val itemStates = _itemStates.asStateFlow()

    protected fun updateItemState(productId: String, update: (ProductUiState) -> ProductUiState) {
        _itemStates.value = _itemStates.value.toMutableMap().apply {
            val currentState = this[productId] ?: ProductUiState(
                id = productId,
                product = Product.empty(productId),
                isFavourite = false,
                cartQuantity = 0)
            this[productId] = update(currentState)
        }
    }

    fun addToFavourite(productId: String) {

    }

    fun addToCart(productId: String, quantity: Int = 1) {

    }
}