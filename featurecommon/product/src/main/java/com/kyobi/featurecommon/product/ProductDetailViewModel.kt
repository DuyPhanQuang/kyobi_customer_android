package com.kyobi.featurecommon.product

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.extension.toUniqueAllProductImages
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
): ViewModel() {
    private val tag = "ProductDetailViewModel"
    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState = _uiState.asStateFlow()

    fun getImageLoader(): ImageLoader = imageLoader

    fun setInitProduct(initProduct: Product) {
        _uiState.value = _uiState.value.copy(
            productResult = DomainNetworkResult.Success(initProduct)
        )
        val preloadProductImages = initProduct.toUniqueAllProductImages()
        viewModelScope.launchOnIO {
            val deferredList = preloadProductImages.map { image ->
                async {
                    processingPreloadImage(image.url)
                }
            }
            deferredList.awaitAll() // parallel
            Timber.tag(tag).d("Preload product images completed")
        }
    }

    private suspend fun processingPreloadImage(imageUrl: String) {
        try {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .crossfade(false)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .allowHardware(true)
                .build()
            Timber.tag(tag).d("Preloading image: $imageUrl")
            imageLoader.execute(request)
            Timber.tag(tag).d("Preload image completed")
        } catch (e: Exception) {
            Timber.tag(tag).e(e, "Preload image failed")
        }
    }
}