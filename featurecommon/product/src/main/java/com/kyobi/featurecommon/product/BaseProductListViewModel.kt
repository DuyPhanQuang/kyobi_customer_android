package com.kyobi.featurecommon.product

import androidx.lifecycle.ViewModel
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
    protected val productsResult = MutableStateFlow<DomainNetworkResult<List<ProductUiState>>>(DomainNetworkResult.Success(emptyList()))
    val products = productsResult.asStateFlow()

    private val itemStatesResult = MutableStateFlow<Map<String, ProductUiState>>(emptyMap())
    val itemStates = itemStatesResult.asStateFlow()

    protected fun updateItemState(productId: String, update: (ProductUiState) -> ProductUiState) {
        itemStatesResult.value = itemStatesResult.value.toMutableMap().apply {
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