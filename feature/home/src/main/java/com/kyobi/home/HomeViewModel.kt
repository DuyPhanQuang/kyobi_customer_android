package com.kyobi.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.usecase.impl.GetProductsUseCaseImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCaseImpl
): ViewModel() {
    private val _products = MutableLiveData<DomainNetworkResult<List<Product>>>()
    val products: LiveData<DomainNetworkResult<List<Product>>> = _products

    init {
        fetchProducts()
    }

    private fun fetchProducts() {
        viewModelScope.launch {
            _products.value = DomainNetworkResult.Success(emptyList())
            val result = getProductsUseCase()
            _products.value = result
        }
    }
}