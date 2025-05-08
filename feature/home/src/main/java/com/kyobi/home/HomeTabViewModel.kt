package com.kyobi.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.model.ShopifyMediaImage
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.model.TopCatalogStatus
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

    private val recommendedReels = listOf(
        LookbookItem("0", "https://media0.giphy.com/media/v1.Y2lkPTc5MGI3NjExaGJmNDA3cXcwaHFvbG9ydHcxM3lmbjlrd2M1cWtvYzlxaHV4Z2k0MiZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/lMsT2f47tDxFMYdJMC/giphy.gif", "#croptop"),
        LookbookItem("1", "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExaWk5c3BsemM4aXFtcHZhb2YzNGF2YXFxMTE4cHRzZHcxeDdvaXhiZyZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/iFmbcbjYR52zChZzQM/giphy.gif", "#dresses"),
        LookbookItem("2", "https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExZ2o2cWl6OTI4bGttY2puN3o5czhudjF6aDU5aHdrenhuMDlraWhmaCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/ZCxO1TRVPO1CL00Z2a/giphy.gif", "#floral"),
        LookbookItem("3", "https://media2.giphy.com/media/v1.Y2lkPTc5MGI3NjExNmdnbGZsOHlpcWNzbTJqb2dlcjVpYXp5ZHFoaGFjNnh2cGtnNTAxbCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/Woi8xNcuMeHB4EGziP/giphy.gif", "#jeans"),
        LookbookItem("4","https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExeWM5enQ4enF6aGRkOThzZ3c3N2R3eHRhbmVoczRyNXVtNW5rYzcweCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/W07gfOm19B7GwoyXMp/giphy.gif", "#jeans"),
        LookbookItem("5","https://media3.giphy.com/media/v1.Y2lkPTc5MGI3NjExdnpwY3g5czM0bjI2bGh3NjBhc214eHZkMWYyb3dsODhrb2gzN3pzMSZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/lMyDzkQUQvDXSDccup/giphy.gif", "#jeans"),
        LookbookItem("6","https://media4.giphy.com/media/v1.Y2lkPTc5MGI3NjExZDQ5anQ0ZHRmcHc5dnRmbGtsbzFkazZjeWRkZWcyc3p5cWoxaDFycCZlcD12MV9pbnRlcm5hbF9naWZfYnlfaWQmY3Q9Zw/dxVJFZRsq41X9nA1YF/giphy.gif", "#jeans"),
    )

    private val topCatalogs = listOf(
        TopCatalog(
            link = "New Arrivals",
            order = 0,
            tag = "new",
            title = "New Arrivals",
            image = ShopifyMedia(
                id = "gid://shopify/MediaImage/27744097992761",
                image = ShopifyMediaImage(
                    altText = null,
                    url = "https://cdn.shopify.com/s/files/1/0713/7184/9785/files/cate-dresses.png?v=1743317462",
                    width = 1024f,
                    height = 1536f
                )
            ),
            status = TopCatalogStatus.active
        ),
        TopCatalog(
            link = "Dresses",
            order = 1,
            tag = "dresses",
            title = "Dresses",
            image = ShopifyMedia(
                id = "gid://shopify/MediaImage/27744097992761",
                image = ShopifyMediaImage(
                    altText = null,
                    url = "https://cdn.shopify.com/s/files/1/0713/7184/9785/files/cate-dresses.png?v=1743317462",
                    width = 1024f,
                    height = 1536f
                )
            ),
            status = TopCatalogStatus.active
        ),
        TopCatalog(
            link = "Tops",
            order = 2,
            tag = "tops",
            title = "Tops",
            image = ShopifyMedia(
                id = "gid://shopify/MediaImage/27744098582585",
                image = ShopifyMediaImage(
                    altText = null,
                    url = "https://cdn.shopify.com/s/files/1/0713/7184/9785/files/cate-tops.png?v=1743317489",
                    width = 1000f,
                    height = 1000f
                )
            ),
            status = TopCatalogStatus.active
        ),
        TopCatalog(
            link = "Shirts & Blouses",
            order = 3,
            tag = "shirts-blouses",
            title = "Shirts & Blouses",
            image = ShopifyMedia(
                id = "gid://shopify/MediaImage/27744098484281",
                image = ShopifyMediaImage(
                    altText = null,
                    url = "https://cdn.shopify.com/s/files/1/0713/7184/9785/files/cate-blouses.png?v=1743317489",
                    width = 600f,
                    height = 600f
                )
            ),
            status = TopCatalogStatus.active
        ),
        TopCatalog(
            link = "Bottoms",
            order = 4,
            tag = "bottoms",
            title = "Bottoms",
            image = ShopifyMedia(
                id = "gid://shopify/MediaImage/27744098615353",
                image = ShopifyMediaImage(
                    altText = null,
                    url = "https://cdn.shopify.com/s/files/1/0713/7184/9785/files/cate-bottoms.png?v=1743317490",
                    width = 2560f,
                    height = 2560f
                )
            ),
            status = TopCatalogStatus.active
        ),
        TopCatalog(
            link = "Coats",
            order = 5,
            tag = "coats",
            title = "Coats",
            image = ShopifyMedia(
                id = "gid://shopify/MediaImage/27744098451513",
                image = ShopifyMediaImage(
                    altText = null,
                    url = "https://cdn.shopify.com/s/files/1/0713/7184/9785/files/cate-coat.png?v=1743317488",
                    width = 225f,
                    height = 225f
                )
            ),
            status = TopCatalogStatus.active
        ),
        TopCatalog(
            link = "Skirts",
            order = 6,
            tag = "skirts",
            title = "Skirts",
            image = ShopifyMedia(
                id = "gid://shopify/MediaImage/27744098517049",
                image = ShopifyMediaImage(
                    altText = null,
                    url = "https://cdn.shopify.com/s/files/1/0713/7184/9785/files/cate-skirts.png?v=1743317489",
                    width = 600f,
                    height = 600f
                )
            ),
            status = TopCatalogStatus.active
        ),
        TopCatalog(
            link = "Jeans",
            order = 7,
            tag = "jeans",
            title = "Jeans",
            image = ShopifyMedia(
                id = "gid://shopify/MediaImage/27744098549817",
                image = ShopifyMediaImage(
                    altText = null,
                    url = "https://cdn.shopify.com/s/files/1/0713/7184/9785/files/cate-denim.png?v=1743317489",
                    width = 600f,
                    height = 600f
                )
            ),
            status = TopCatalogStatus.active
        ),
        TopCatalog(
            link = "Matching Sets",
            order = 8,
            tag = "matching-sets",
            title = "Matching Sets",
            image = ShopifyMedia(
                id = "gid://shopify/MediaImage/27744104120377",
                image = ShopifyMediaImage(
                    altText = null,
                    url = "https://cdn.shopify.com/s/files/1/0713/7184/9785/files/matching_set.jpg?v=1743317895",
                    width = 780f,
                    height = 569f
                )
            ),
            status = TopCatalogStatus.active
        )
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
        fetchProducts()
    }

    fun getImageLoader(): ImageLoader = imageLoader

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
                Timber.tag(tag).d("Preload banner images completed in $duration ms")
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

    fun getTopCatalog(): List<TopCatalog> = topCatalogs

    private fun fetchTopCatalog() {
    }

    fun getProductDeals(): List<ProductItem> = productDeals

    private fun fetchProductDeals() {
        viewModelScope.launchOnIO {
            val startTime = System.currentTimeMillis()
            try {
                val deferredList = productDeals.map { product ->
                    async {
                        val request = ImageRequest.Builder(context)
                            .data(product.imageUrl)
                            .memoryCachePolicy(CachePolicy.ENABLED)
                            .diskCachePolicy(CachePolicy.ENABLED)
                            .build()
                        imageLoader.execute(request)
                    }
                }
                deferredList.awaitAll() // parallel
                val duration = System.currentTimeMillis() - startTime
                Timber.tag(tag).d("Preload product deal images completed in $duration ms")
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Preload banner images failed")
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