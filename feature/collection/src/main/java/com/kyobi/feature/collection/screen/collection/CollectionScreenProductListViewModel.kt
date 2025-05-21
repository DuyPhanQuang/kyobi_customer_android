package com.kyobi.feature.collection.screen.collection

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
import kotlinx.coroutines.flow.collectLatest
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionScreenProductListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase,
    addToCartUseCase: AddToCartUseCase,
    addRemoveProductToFavoriteUseCase: AddRemoveProductToFavoriteUseCase,
): BaseProductListViewModel(
    addToCartUseCase,
    addRemoveProductToFavoriteUseCase
) {
    private val tag = "CollectionProductListViewModel"
    private lateinit var eventBus: CollectionScreenEventBus

    fun initWithEventBus(initEventBus: CollectionScreenEventBus) {
        this.eventBus = initEventBus
        viewModelScope.launchOnIO {
            eventBus.events.collect { event ->
                when (event) {
                    is CollectionScreenEvent.CollectionSelected -> {
                        val filterHandle = event.filterHandle
                        Timber.tag(tag).d("Received CollectionSelected event with filterHandle: $filterHandle")
                        if (filterHandle == null) return@collect
                        fetchProductsByCollection(filterHandle)
                    }
                }
            }
        }
    }

    private fun fetchProductsByCollection(filterHandle: String) {
        viewModelScope.launchOnIO {
            productsResult.value = DomainNetworkResult.Loading
            try {
                getProductsUseCase.invoke(
                    query = filterHandle.toQueryBySingleTag(),
                    reverse = null,
                    sortKey = null,
                    identifiers = null,
                    first = 250
                ).collectLatest { result ->
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
                Timber.tag(tag).e(e, "Failed to fetch products by collection: $filterHandle")
            }
        }
    }
}