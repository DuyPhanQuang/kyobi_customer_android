package com.kyobi.featurecommon.product

import androidx.lifecycle.ViewModel
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val imageLoader: ImageLoader,
): ViewModel() {
    private val tag = "ProductDetailViewModel"
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun getImageLoader(): ImageLoader = imageLoader
}