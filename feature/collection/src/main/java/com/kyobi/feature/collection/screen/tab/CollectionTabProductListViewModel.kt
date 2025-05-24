package com.kyobi.feature.collection.screen.tab

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
import kotlinx.coroutines.launch
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
    private lateinit var eventBus: CollectionTabEventBus

    init {
        fetchAllProducts()
    }

    fun initWithEventBus(initEventBus: CollectionTabEventBus) {
        this.eventBus = initEventBus
        viewModelScope.launch {
            eventBus.events.collect { event ->
                Timber.tag(tag).d("***CollectionTabEventBus*** subscribed event: $event")
                when (event) {
                    is CollectionTabEvent.CategorySelected -> {
                        processingRequestCategorySelected(event)
                        return@collect
                    }
                    is CollectionTabEvent.SubCategorySelected -> {
                        processingRequestSubCategorySelected(event)
                        return@collect
                    }
                }
            }
        }
    }

    private fun processingRequestCategorySelected(event: CollectionTabEvent.CategorySelected) {
        val filterHandle = event.filterHandle
        Timber.tag(tag).d("Received CategorySelected event with filterHandle: $filterHandle")
        fetchProductsByCollection(filterHandle)
    }

    private fun processingRequestSubCategorySelected(event: CollectionTabEvent.SubCategorySelected) {
        val filterHandle = event.filterHandle
        Timber.tag(tag).d("Received SubCategorySelected event with filterHandle: $filterHandle")
        fetchProductsByCollection(filterHandle)
    }

    private fun fetchAllProducts() {
        fetchProductsByCollection(null)
    }

    private fun fetchProductsByCollection(filterHandle: String?) {
        viewModelScope.launchOnIO {
            val queryTag = filterHandle ?: "women"
            getProductsUseCase.invoke(
                query = queryTag.toQueryBySingleTag(),
                reverse = null,
                sortKey = null,
                identifiers = null,
                first = 250
            ).collect { result ->
                Timber.tag(tag).d("Processing fetchProductsByCollection result: $result")
                when (result) {
                    is DomainNetworkResult.Success -> {
                        productsResult.value = DomainNetworkResult.Success(
                            result.data.map { ProductUiState.fromProduct(it) }
                        )
                    }
                    is DomainNetworkResult.Loading -> {
                        productsResult.value = result
                    }
                    is DomainNetworkResult.Error -> {
                        productsResult.value = result
                    }
                }
            }
        }
    }
}