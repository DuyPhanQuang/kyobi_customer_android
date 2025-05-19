package com.kyobi.trend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import coil.ImageLoader
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrendTabViewModel @Inject constructor(
    private val imageLoader: ImageLoader,
): ViewModel() {
    var trendTabUiState by mutableStateOf(TrendTabUiState())
        private set

    fun getImageLoader(): ImageLoader = imageLoader
}