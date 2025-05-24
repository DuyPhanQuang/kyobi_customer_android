package com.kyobi.feature.collection.screen.collection

import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.core.coroutines.launchOnMain
import com.kyobi.core.extensions.toQueryBySingleTag
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.request.MetafieldIdentifierRequest
import com.kyobi.domain.usecase.AddRemoveProductToFavoriteUseCase
import com.kyobi.domain.usecase.AddToCartUseCase
import com.kyobi.domain.usecase.GetProductsUseCase
import com.kyobi.feature.collection.extension.colorDefaultKey
import com.kyobi.feature.collection.extension.colorPattenKey
import com.kyobi.feature.collection.extension.prefixFilterKey
import com.kyobi.feature.collection.model.FilterOption
import com.kyobi.featurecommon.product.BaseProductListViewModel
import com.kyobi.featurecommon.product.ProductUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
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
        viewModelScope.launch {
            eventBus.events.collect { event ->
                Timber.tag(tag).d("***CollectionScreenEventBus*** subscribed event: $event")
                when (event) {
                    is CollectionScreenEvent.CollectionSelected -> {
                        processingRequestCollectionSelected(event)
                        return@collect
                    }
                    is CollectionScreenEvent.FilterOptionsSelected -> {
                        processingRequestFilterOptionsSelected(event)
                        return@collect
                    }
                }
            }
        }
    }

    private fun processingRequestCollectionSelected(event: CollectionScreenEvent.CollectionSelected) {
        val eventData = event.filterHandle
        Timber.tag(tag).d("Received CollectionSelected event with filterHandle: $eventData")
        if (eventData == null) return
        fetchProductsByCollection(eventData)
    }

    private fun processingRequestFilterOptionsSelected(event: CollectionScreenEvent.FilterOptionsSelected) {
        val optionsData = event.options
        val handleData = event.filterHandle
        Timber.tag(tag).d("Received FilterOptionsSelected event with options: $optionsData, handle: $handleData")
        fetchProductsByCollectionWithFilterKeys(optionsData, handleData)
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

    private fun fetchProductsByCollectionWithFilterKeys(options: List<FilterOption>, filterHandle: String?) {
        val finalTag = filterHandle ?: "women"
        val filterKeys = options.mapNotNull { it.key }.distinct()
        val identifiers = mutableListOf<MetafieldIdentifierRequest>()
        filterKeys.forEach { key ->
            val formattedKey = key.removePrefix("${prefixFilterKey}.").let {
                if (it == colorDefaultKey) colorPattenKey else it
            }
            identifiers.add(
                MetafieldIdentifierRequest(
                    namespace = "kyobi",
                    key = formattedKey)
            )
        }
        viewModelScope.launchOnIO {
            getProductsUseCase.invoke(
                query = finalTag.toQueryBySingleTag(),
                reverse = null,
                sortKey = null,
                identifiers = identifiers,
                first = 250
            ).collect { result ->
                Timber.tag(tag).d("Processing fetchProductsByCollectionWithFilterKeys result: $result")
                when (result) {
                    is DomainNetworkResult.Success -> {
                        val filteredProducts = filterProductsByCondition(result.data, options)
                        val finalProductUiStates = filteredProducts.map { ProductUiState.fromProduct(it) }
                        productsResult.value = DomainNetworkResult.Success(finalProductUiStates)
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

    private fun filterProductsByCondition(products: List<Product>, filters: List<FilterOption>): List<Product> {
        // create map từ filters: key -> Set<value>
        val filterMap = filters.groupBy { it.key ?: "" }
            .mapValues { entry -> entry.value.map { it.label }.toSet() }
            .filter { it.value.isNotEmpty() }
        if (filterMap.isEmpty()) return products
        return products.filter { product ->
            // get metafields của product và xây dựng map: key -> Set<label>
            val productFilters = mutableMapOf<String, MutableSet<String>>()
            product.metafields?.forEach { metafield ->
                // filter key
                var metafieldKey = metafield.key
                // case: key is color
                if (metafieldKey == colorDefaultKey) {
                    metafieldKey = colorPattenKey
                }
                metafield.references?.nodes?.forEach { node ->
                    val labelField = node.fields?.find { it.key == "label" }
                    labelField?.value?.let { label ->
                        productFilters.getOrPut("$prefixFilterKey.$metafieldKey") { mutableSetOf() }.add(label)
                    }
                }
            }
            // Kiểm tra từng key trong filterMap
            filterMap.all { (key, allowedLabels) ->
                val productLabels = productFilters[key] ?: emptySet()
                allowedLabels.any { it in productLabels }
            }
        }
    }
}