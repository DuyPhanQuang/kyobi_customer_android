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

const val collectionDefault = "women"

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
        fetchProductsByCollection(collectionDefault)
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
                    is CollectionTabEvent.RefreshTriggered -> {
                        processingRequestRefreshTriggered(event)
                        return@collect
                    }
                }
            }
        }
    }

    private fun processingRequestCategorySelected(event: CollectionTabEvent.CategorySelected) {
        Timber.tag(tag).d("Received CategorySelected event with filterHandle: ${event.filterHandle}")
        val filterHandle = event.filterHandle ?: return
        fetchProductsByCollection(filterHandle)
    }

    private fun processingRequestSubCategorySelected(event: CollectionTabEvent.SubCategorySelected) {
        Timber.tag(tag).d("Received SubCategorySelected event with filterHandle: ${event.filterHandle}")
        val filterHandle = event.filterHandle ?: return
        fetchProductsByCollection(filterHandle)
    }

    private fun processingRequestRefreshTriggered(event: CollectionTabEvent.RefreshTriggered) {
        Timber.tag(tag).d("Received RefreshTriggered event with filterHandle: ${event.filterHandle}")
        val handle = event.filterHandle ?: collectionDefault
        fetchProductsByCollection(handle)
    }

    private fun fetchProductsByCollection(filterHandle: String) {
        viewModelScope.launchOnIO {
            getProductsUseCase.invoke(
                query = filterHandle.toQueryBySingleTag(),
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