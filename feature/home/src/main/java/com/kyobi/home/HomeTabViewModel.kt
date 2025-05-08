package com.kyobi.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.usecase.GetProductsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeTabViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getProductsUseCase: GetProductsUseCase,
    private val imageLoader: ImageLoader
): ViewModel() {
    private val tag = "HomeTabViewModel"
    private val _uiState = MutableStateFlow(HomeTabUiState())
    val uiState = _uiState.asStateFlow()
    private val banners = listOf(
        "https://images.unsplash.com/photo-1506157786151-b8491531f063",
        "https://images.unsplash.com/photo-1511556820780-d912e42b4980",
        "https://images.unsplash.com/photo-1483985988355-763728e1935b"
    )

    init {
        fetchBanners()
        fetchProducts()
    }

    fun getBanners(): List<String> = banners

    private fun fetchBanners() {
        viewModelScope.launchOnIO {
            val startTime = System.currentTimeMillis()
            try {
                val deferredList = banners.map { url ->
                    async {
                        val request = ImageRequest.Builder(context)
                            .data(url)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                        imageLoader.execute(request)
                    }
                }
                deferredList.awaitAll() // parallel
                val duration = System.currentTimeMillis() - startTime
                Timber.tag(tag).d("Preload completed in $duration ms")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Preload failed")
            }
        }
    }

    private fun fetchProducts() {
        viewModelScope.launchOnIO {
            getProductsUseCase.invoke(
                query = null,
                reverse = null,
                sortKey = null,
                identifiers = null,
                first = null,
            ).collect { result ->
                _uiState.value = _uiState.value.copy(
                    productsResult = result)
            }
        }
    }
}