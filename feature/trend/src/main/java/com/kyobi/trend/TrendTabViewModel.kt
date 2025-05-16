package com.kyobi.trend

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrendTabViewModel @Inject constructor(): ViewModel() {
    var trendTabUiState by mutableStateOf(TrendTabUiState())
        private set
}