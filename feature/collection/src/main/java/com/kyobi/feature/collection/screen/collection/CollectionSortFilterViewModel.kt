package com.kyobi.feature.collection.screen.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.feature.collection.model.FilterOption
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionSortFilterViewModel @Inject constructor(): ViewModel() {
    private val tag = "CollectionSortFilterViewModel"
    private val _uiState = MutableStateFlow(CollectionSortFilterUiState(selectedFilterOptions = emptyList()))
    val uiState = _uiState.asStateFlow()
    private lateinit var eventBus: CollectionScreenEventBus

    fun initWithEventBus(initEventBus: CollectionScreenEventBus) {
        this.eventBus = initEventBus
    }

    /** Step1: xử lý logic select/unselect filter options selected
     * Step2: emit event cho product list view model để fetch products with filter keys
     * */
    fun toggleFilterOption(filterOption: FilterOption, currentFilterHandle: String?) {
        val currentData = _uiState.value.selectedFilterOptions
        val newData = currentData.toMutableList().apply {
            val existingIndex = indexOfFirst { it.label == filterOption.label }
            if (existingIndex != -1) {
                removeAt(existingIndex) // Unselect
            } else {
                add(filterOption) // Select
            }
        }
        _uiState.value = _uiState.value.copy(selectedFilterOptions = newData)
        requestFetchProductsWithFilterKeys(
            options = newData,
            filterHandle = currentFilterHandle)
    }

    /** Step1: xử lý logic clear hết data filter options selected
     * Step2: emit event cho product list view model để fetch products with filter keys ngoại trừ `key`
     * */
    fun clearFilterOptions(key: String, currentFilterHandle: String?) {
        val currentData = _uiState.value.selectedFilterOptions
        val newData = currentData.filter { it.key != key }
        _uiState.value = _uiState.value.copy(selectedFilterOptions = newData)
        requestFetchProductsWithFilterKeys(
            options = newData,
            filterHandle = currentFilterHandle)
    }

    private fun requestFetchProductsWithFilterKeys(options: List<FilterOption>, filterHandle: String?) {
        viewModelScope.launchOnIO {
            eventBus.emitEvent(CollectionScreenEvent.FilterOptionsSelected(options, filterHandle))
            Timber.tag(tag).d("Emitted FilterOptionsSelected event with options: $options")
        }
    }
}