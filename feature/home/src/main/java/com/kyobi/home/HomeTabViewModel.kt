package com.kyobi.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.domain.usecase.impl.GetProductsUseCaseImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeTabViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCaseImpl
): ViewModel() {
    private val _uiState = MutableStateFlow(HomeTabUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            getProductsUseCase().collect { result ->
                _uiState.value = _uiState.value.copy(
                    productsResult = result)
            }
        }
    }
}