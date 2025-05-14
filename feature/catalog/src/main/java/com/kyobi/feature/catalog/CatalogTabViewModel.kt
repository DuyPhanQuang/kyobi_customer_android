package com.kyobi.feature.catalog

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CatalogTabViewModel @Inject constructor(): ViewModel() {
    private val _uiState = MutableStateFlow(CatalogTabUiState())
    val uiState = _uiState.asStateFlow()
}