package com.kyobi.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.usecase.GetFlashSaleUseCase
import com.kyobi.domain.usecase.GetHomePagesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeTabViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getHomePagesUseCase: GetHomePagesUseCase,
    private val getFlashSaleUseCase: GetFlashSaleUseCase,
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

    init {
        fetchBanners()
        fetchRecommendedReels()
        fetchTopCatalog()
        fetchProductDeals()
        fetchSaleProducts()
        fetchTrendingResearchs()
    }

    fun getImageLoader(): ImageLoader = imageLoader

    fun onRefreshTriggered(onCompleted: () -> Unit) {
        try {
            fetchBanners()
            fetchRecommendedReels()
            fetchTopCatalog()
            fetchProductDeals()
            fetchSaleProducts()
            fetchTrendingResearchs()
        } finally {
            onCompleted()
        }
    }

    private fun fetchBanners() {
        viewModelScope.launchOnIO {
            try {
                getHomePagesUseCase.getHomeBanners().collect { result ->
                    _uiState.value = _uiState.value.copy(bannersResult = result)
                    if (result is DomainNetworkResult.Success) {
                        val banners = result.data
                        val deferredList = banners.mapNotNull { banner ->
                            val imageData = banner.image?.image
                            imageData?.let {
                                async {
                                    processingPreloadImage(it.url)
                                }
                            }
                        }
                        deferredList.awaitAll() // parallel
                        Timber.tag(tag).d("Preload banner images completed")
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
            try {
                val deferredList = recommendedReels.take(3).map { reel ->
                    async {
                        processingPreloadImage(reel.imageUrl)
                    }
                }
                deferredList.awaitAll() // parallel
                Timber.tag(tag).d("Preload recommended reel images completed")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Preload recommended reel images failed")
            }
        }
    }

    private fun fetchTopCatalog() {
        viewModelScope.launchOnIO {
            try {
                getHomePagesUseCase.getHomeTopCatalogs().collect { result ->
                    _uiState.value = _uiState.value.copy(topCatalogsResult = result)
                    if (result is DomainNetworkResult.Success) {
                        val topCatalogs = result.data
                        val deferredList = topCatalogs.mapNotNull { topCatalog ->
                            val imageData = topCatalog.image?.image
                            imageData?.let {
                                async {
                                    processingPreloadImage(it.url)
                                }
                            }
                        }
                        deferredList.awaitAll() // parallel
                        Timber.tag(tag).d("Preload top catalog images completed")
                    }
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Preload top catalog images failed")
            }
        }
    }

    private fun fetchProductDeals() {
        val dummyHandle = "flash-sale-disco"
        viewModelScope.launchOnIO {
            getFlashSaleUseCase.getFlashSale(
                handle = dummyHandle
            ).collect { result ->
                _uiState.value = _uiState.value.copy(flashSaleResult = result)
                if (result is DomainNetworkResult.Success) {
                    val flashSaleData = result.data
                    val imageData = flashSaleData.flashSaleInfo.background?.image
                    imageData?.let {
                        processingPreloadImage(it.url)
                    }
                }
            }
        }
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