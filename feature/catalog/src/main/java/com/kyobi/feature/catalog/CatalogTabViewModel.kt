package com.kyobi.feature.catalog

import android.content.Context
import androidx.lifecycle.ViewModel
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CatalogTabViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader
): ViewModel() {
    private val _uiState = MutableStateFlow(CatalogTabUiState())
    val uiState = _uiState.asStateFlow()

    fun getImageLoader(): ImageLoader = imageLoader
}