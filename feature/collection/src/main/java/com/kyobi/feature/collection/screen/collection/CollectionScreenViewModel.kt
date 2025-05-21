package com.kyobi.feature.collection.screen.collection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.core.extensions.toFirstGid
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.usecase.GetShopifyMediaUseCase
import com.kyobi.feature.collection.screen.collection.model.CollectionMenu
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getShopifyMediaUseCase: GetShopifyMediaUseCase,
    private val imageLoader: ImageLoader,
    private val collectionScreenEventBus: CollectionScreenEventBus
): ViewModel() {
    private val tag = "CollectionViewModel"
    private val _uiState = MutableStateFlow(CollectionScreenUiState(emptyList()))
    val uiState = _uiState.asStateFlow()

    fun setCollectionMenus(data: List<CollectionMenu>) {
        _uiState.value = _uiState.value.copy(collectionMenus = data)
        fetchImagesThenUpdateCollectionMenus(data)
    }

    fun getEventBus(): CollectionScreenEventBus = collectionScreenEventBus

    fun getImageLoader(): ImageLoader = imageLoader

    private fun fetchImagesThenUpdateCollectionMenus(currentCollectionMenus: List<CollectionMenu>) {
        viewModelScope.launchOnIO {
            val thumbnailInfoCache = mutableMapOf<String, ShopifyMedia>()
            val idsToFetch = currentCollectionMenus
                .mapNotNull { item -> item.thumbnail?.toFirstGid() }
                .filter { id -> id !in thumbnailInfoCache }
            try {
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
                        is DomainNetworkResult.Error -> {}
                        is DomainNetworkResult.Loading -> {}
                    }
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "fetch images and update collection menus failed")
            }
        }
    }

    fun updateCollectionSelected(itemSelected: CollectionMenu) {
        if (itemSelected.id == _uiState.value.selectedCollectionId) return
        _uiState.value = _uiState.value.copy(selectedCollectionId = itemSelected.id)
        viewModelScope.launchOnIO {
            collectionScreenEventBus.emitEvent(CollectionScreenEvent.CollectionSelected(itemSelected.filterHandle))
            Timber.tag(tag).d("Emitted CollectionSelected event with filterHandle: ${itemSelected.filterHandle}")
        }
    }

    fun fetchProductByCollectionDefault() {
        val collectionDefaultConfig = "women"
        viewModelScope.launchOnIO {
            collectionScreenEventBus.emitEvent(CollectionScreenEvent.CollectionSelected(collectionDefaultConfig))
            Timber.tag(tag).d("Emitted CollectionSelected event with filterHandle: $collectionDefaultConfig")
        }
    }
}