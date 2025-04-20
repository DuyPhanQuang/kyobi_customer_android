package com.kyobi.trend

import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import com.kyobi.trend.cache.MediaCache
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TrendTabViewModel
@OptIn(UnstableApi::class)
@Inject constructor(
    val mediaCache: MediaCache
): ViewModel() {
    var trendTabUiState by mutableStateOf(TrendTabUiState())
        private set

    @OptIn(UnstableApi::class)
    override fun onCleared() {
        mediaCache.release()
        super.onCleared()
    }
}