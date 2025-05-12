package com.kyobi.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.usecase.GetHomePagesUseCase
import com.kyobi.domain.usecase.GetProductRecommendationsUseCase
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
    private val getHomePagesUseCase: GetHomePagesUseCase,
    private val getProductRecommendationsUseCase: GetProductRecommendationsUseCase,
    private val imageLoader: ImageLoader
): ViewModel() {
    private val tag = "HomeTabViewModel"
    private val _uiState = MutableStateFlow(HomeTabUiState())
    val uiState = _uiState.asStateFlow()

    private val recommendedReels = listOf(
        LookbookItem("0", "https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExaGJmNDA3cXcwaHFvbG9ydHcxM3lmbjlrd2M1cWtvYzlxaHV4Z2k0MiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/lMsT2f47tDxFMYdJMC/giphy.gif", "#croptop"),
        LookbookItem("1", "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExaWk5c3BsemM4aXFtcHZhb2YzNGF2YXFxMTE4cHRzZHcxeDdvaXhiZyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/iFmbcbjYR52zChZzQM/giphy.gif", "#dresses"),
        LookbookItem("2", "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExZ2o2cWl6OTI4bGttY2puN3o5czhudjF6aDU5aHdrenhuMDlraWhmaCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/ZCxO1TRVPO1CL00Z2a/giphy.gif", "#floral"),
        LookbookItem("3", "https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExNmdnbGZsOHlpcWNzbTJqb2dlcjVpYXp5ZHFoaGFjNnh2cGtnNTAxbCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/Woi8xNcuMeHB4EGziP/giphy.gif", "#jeans"),
        LookbookItem("4","https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExeWM5enQ4enF6aGRkOThzZ3c3N2R3eHRhbmVoczRyNXVtNW5rYzcweCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/W07gfOm19B7GwoyXMp/giphy.gif", "#jeans"),
        LookbookItem("5","https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExdnpwY3g5czM0bjI2bGh3NjBhc214eHZkMWYyb3dsODhrb2gzN3pzMSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/lMyDzkQUQvDXSDccup/giphy.gif", "#jeans"),
        LookbookItem("6","https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExZDQ5anQ0ZHRmcHc5dnRmbGtsbzFkazZjeWRkZWcyc3p5cWoxaDFycCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/dxVJFZRsq41X9nA1YF/giphy.gif", "#jeans"),
    )

    private val productDeals = listOf(
        ProductItem("1", "https://images.unsplash.com/photo-1506157786151-b8491531f063", "19.90"),
        ProductItem("2", "https://images.unsplash.com/photo-1511556820780-d912e42b4980", "59.90"),
        ProductItem("3", "https://images.unsplash.com/photo-1507525428034-b723cf961d3e", "49.90"),
        ProductItem("4", "https://images.unsplash.com/photo-1483985988355-763728e1935b", "29.90"),
        ProductItem("5", "https://images.unsplash.com/photo-1536514498073-50e69d39c6cf", "29.90")
    )

    init {
        fetchBanners()
        fetchRecommendedReels()
        fetchTopCatalog()
        fetchProductDeals()
        fetchSaleProducts()
        fetchTrendingResearchs()
        fetchProductRecommendations(emptyList(), emptyList())
    }

    fun getImageLoader(): ImageLoader = imageLoader

    private fun fetchBanners() {
        viewModelScope.launchOnIO {
            try {
                getHomePagesUseCase.getHomeBanners().collect { result ->
                    _uiState.value = _uiState.value.copy(bannersResult = result)
                    if (result is DomainNetworkResult.Success) {
                        val banners = result.data
                        val startTime = System.currentTimeMillis()
                        val deferredList = banners.mapNotNull { banner ->
                            val imageData = banner.image?.image
                            imageData?.let { url ->
                                async {
                                    val request = ImageRequest.Builder(context)
                                        .data(url)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .build()
                                    Timber.tag(tag).d("Preloading image: $url")
                                    imageLoader.execute(request)
                                }
                            }
                        }
                        deferredList.awaitAll() // parallel
                        val duration = System.currentTimeMillis() - startTime
                        Timber.tag(tag).d("Preload banner images completed in $duration ms")
                    }
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Preload banner images failed")
            }
        }
    }

    fun getRecommendedReels(): List<LookbookItem> = recommendedReels

    private fun fetchRecommendedReels() {
        viewModelScope.launchOnIO {
            val startTime = System.currentTimeMillis()
            try {
                val deferredList = recommendedReels.take(3).map { reel ->
                    async {
                        val request = ImageRequest.Builder(context)
                            .data(reel.imageUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .allowHardware(false)
                            .build()
                        val result = imageLoader.execute(request)
                        Timber.tag(tag).d("Preload reel: ${reel.imageUrl}, success: ${result is coil.request.SuccessResult}")
                        result
                    }
                }
                deferredList.awaitAll() // parallel
                val duration = System.currentTimeMillis() - startTime
                Timber.tag(tag).d("Preload recommended reel images completed in $duration ms")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Preload recommended reel images failed")
            }
        }
    }

    private fun fetchTopCatalog() {
        viewModelScope.launchOnIO {
            getHomePagesUseCase.getHomeTopCatalogs().collect { result ->
                _uiState.value = _uiState.value.copy(topCatalogsResult = result)
                if (result is DomainNetworkResult.Success) {
                    val topCatalogs = result.data
                    val startTime = System.currentTimeMillis()
                    val deferredList = topCatalogs.mapNotNull { topCatalog ->
                        val imageData = topCatalog.image?.image
                        imageData?.let { url ->
                            async {
                                val request = ImageRequest.Builder(context)
                                    .data(url)
                                    .memoryCachePolicy(CachePolicy.ENABLED)
                                    .diskCachePolicy(CachePolicy.ENABLED)
                                    .build()
                                Timber.tag(tag).d("Preloading image: $url")
                                imageLoader.execute(request)
                            }
                        }
                    }
                    deferredList.awaitAll() // parallel
                    val duration = System.currentTimeMillis() - startTime
                    Timber.tag(tag).d("Preload top catalog images completed in $duration ms")
                }
            }
        }
    }

    fun getProductDeals(): List<ProductItem> = productDeals

    private fun fetchProductDeals() {

    }

    private fun fetchSaleProducts() {
        viewModelScope.launchOnIO {
            getHomePagesUseCase.getHomeSaleProducts().collect { result ->
                _uiState.value = _uiState.value.copy(saleProductsResult = result)
            }
        }
    }

    private fun fetchTrendingResearchs() {
        viewModelScope.launchOnIO {
            getHomePagesUseCase.getHomeTrendingResearchs().collect { result ->
                _uiState.value = _uiState.value.copy(trendingResearchResult = result)
            }
        }
    }

    private fun fetchProductRecommendations(
        cartProductIds: List<String>,
        recentlyViewedProductIds: List<String>
    ) {
        viewModelScope.launchOnIO {
            _uiState.value = _uiState.value.copy(
                recommendedProductsResult = DomainNetworkResult.Loading)
            try {
                val allProductIds = (cartProductIds + recentlyViewedProductIds).distinct()
                val recommendedProducts = mutableListOf<Product>()
                // Limit to 10 ids
                for (productId in allProductIds.take(10)) {
                    getProductRecommendationsUseCase.invoke(productId).collect { result ->
                        if (result is DomainNetworkResult.Success) {
                            result.data.forEach { product ->
                                if (!recommendedProducts.any { it.id == product.id }) {
                                    recommendedProducts.add(product)
                                }
                            }
                        }
                    }
                }
                if (recommendedProducts.isEmpty()) {
                    getProductsUseCase.invoke(
                        query = "tag:women",
                        reverse = null,
                        sortKey = null,
                        identifiers = null,
                        first = null,
                    ).collect { result ->
                        _uiState.value = _uiState.value.copy(
                            recommendedProductsResult = result)
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        recommendedProductsResult = DomainNetworkResult.Success(recommendedProducts)
                    )
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Failed to fetch product recommendations")
                _uiState.value = _uiState.value.copy(
                    recommendedProductsResult = DomainNetworkResult.Error.Generic(e)
                )
            }
        }
    }
}