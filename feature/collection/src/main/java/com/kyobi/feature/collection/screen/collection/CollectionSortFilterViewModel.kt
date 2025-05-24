package com.kyobi.feature.collection.screen.collection

import android.content.Context
import androidx.lifecycle.ViewModel
import com.kyobi.feature.collection.model.FilterOption
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CollectionSortFilterViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
): ViewModel() {
    private val tag = "CollectionSortFilterViewModel"
    private val _uiState = MutableStateFlow(CollectionSortFilterUiState(selectedFilterOptions = emptyList()))
    val uiState = _uiState.asStateFlow()
    private lateinit var eventBus: CollectionScreenEventBus

    fun initWithEventBus(initEventBus: CollectionScreenEventBus) {
        this.eventBus = initEventBus
    }

    fun toggleFilterOption(filterOption: FilterOption) {
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
    }

    fun clearFilterOptions(key: String) {
        val currentData = _uiState.value.selectedFilterOptions
        val newData = currentData.filter { it.key != key }
        _uiState.value = _uiState.value.copy(selectedFilterOptions = newData)
    }
}