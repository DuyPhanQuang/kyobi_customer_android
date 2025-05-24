package com.kyobi.feature.collection.screen.collection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.core.coroutines.launchOnMain
import com.kyobi.core.extensions.toFirstGid
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.usecase.GetFilterSetUseCase
import com.kyobi.domain.usecase.GetShopifyMediaUseCase
import com.kyobi.feature.collection.screen.collection.model.CollectionMenu
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionScreenViewModel @Inject constructor(
    private val getShopifyMediaUseCase: GetShopifyMediaUseCase,
    private val getFilterSetUseCase: GetFilterSetUseCase,
    private val imageLoader: ImageLoader,
): ViewModel() {
    private val tag = "CollectionViewModel"
    private val _uiState = MutableStateFlow(CollectionScreenUiState(collectionMenus = emptyList()))
    val uiState = _uiState.asStateFlow()
    private lateinit var eventBus: CollectionScreenEventBus

    fun initWithEventBus(initEventBus: CollectionScreenEventBus) {
        this.eventBus = initEventBus
    }

    fun setCollectionMenus(data: List<CollectionMenu>) {
        _uiState.value = _uiState.value.copy(collectionMenus = data)
        fetchImagesThenUpdateCollectionMenus(data)
    }

    fun getImageLoader(): ImageLoader = imageLoader

    private fun fetchImagesThenUpdateCollectionMenus(currentCollectionMenus: List<CollectionMenu>) {
        viewModelScope.launchOnIO {
            val thumbnailInfoCache = mutableMapOf<String, ShopifyMedia>()
            val idsToFetch = currentCollectionMenus
                .mapNotNull { item -> item.thumbnail?.toFirstGid() }
                .filter { id -> id !in thumbnailInfoCache }
            getShopifyMediaUseCase.getImagesByIds(idsToFetch).collect { result ->
                when (result) {
                    is DomainNetworkResult.Success -> {
                        val mediaImages = result.data
                        mediaImages.forEach { mediaImage -> thumbnailInfoCache[mediaImage.id] = mediaImage }
                        val updatedCollectionMenus = _uiState.value.collectionMenus.map { collectionMenu ->
                            val imageId = collectionMenu.thumbnail?.toFirstGid()
                            if (imageId != null && thumbnailInfoCache.containsKey(imageId)) {
                                collectionMenu.copy(thumbnailInfo = thumbnailInfoCache[imageId])
                            } else { collectionMenu }
                        }
                        _uiState.value = _uiState.value.copy(collectionMenus = updatedCollectionMenus)
                    }
                    is DomainNetworkResult.Error -> return@collect
                    is DomainNetworkResult.Loading -> return@collect
                }
            }
        }
    }

    /** Step1: update collection selected
     * Step2: fetch list product theo collection selected
     * Step3: fetch filter set theo collection selected
     * */
    fun updateCollectionSelected(itemSelected: CollectionMenu) {
        _uiState.value = _uiState.value.copy(
            selectedCollectionId = itemSelected.id,
            selectedFilterHandle = itemSelected.filterHandle
        )
        viewModelScope.launch {
            eventBus.emitEvent(CollectionScreenEvent.CollectionSelected(itemSelected.filterHandle))
            Timber.tag(tag).d("Emitted CollectionSelected event with filterHandle: ${itemSelected.filterHandle}")
        }
        val cateHandle = itemSelected.filterHandle
        fetchCateFilterByCollection(cateHandle)
    }

    /** Step1: fetch list product theo collection default
     * Step3: fetch filter set theo collection default
     * */
    fun updateNonCollectionSelect() {
        fetchProductByCollectionDefault()
        fetchCateFilterByCollectionDefault()
    }

    private fun fetchProductByCollectionDefault() {
        val collectionDefaultConfig = "women"
        viewModelScope.launch {
            eventBus.emitEvent(CollectionScreenEvent.CollectionSelected(collectionDefaultConfig))
            Timber.tag(tag).d("Emitted CollectionSelected event with filterHandle: $collectionDefaultConfig")
        }
    }

    private fun fetchCateFilterByCollectionDefault() {
        viewModelScope.launchOnIO {
            getFilterSetUseCase.getFilterSetByDefault().collect { result ->
                when (result) {
                    is DomainNetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(cateFilter = result.data)
                    }
                    is DomainNetworkResult.Error -> {
                        _uiState.value = _uiState.value.copy(cateFilter = null)
                    }
                    is DomainNetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(cateFilter = null)
                    }
                }
            }
        }
    }

    private fun fetchCateFilterByCollection(cateHandle: String) {
        viewModelScope.launchOnIO {
            getFilterSetUseCase.getFilterSetByCateHandle(cateHandle).collect { result ->
                when (result) {
                    is DomainNetworkResult.Success -> {
                        _uiState.value = _uiState.value.copy(cateFilter = result.data)
                    }
                    is DomainNetworkResult.Error -> {
                        // switch to fallback collection default
                        fetchCateFilterByCollectionDefault()
                    }
                    is DomainNetworkResult.Loading -> {
                        _uiState.value = _uiState.value.copy(cateFilter = null)
                    }
                }
            }
        }
    }
}