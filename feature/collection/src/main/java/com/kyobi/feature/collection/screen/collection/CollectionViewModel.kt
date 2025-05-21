package com.kyobi.feature.collection.screen.collection

import android.content.Context
import androidx.lifecycle.ViewModel
import coil.ImageLoader
import com.kyobi.feature.collection.screen.collection.model.CollectionMenu
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val imageLoader: ImageLoader,
): ViewModel() {
    private val tag = "CollectionViewModel"
    private val _uiState = MutableStateFlow(CollectionUiState(emptyList()))
    val uiState = _uiState.asStateFlow()

    fun setCollectionMenus(data: List<CollectionMenu>) {
        _uiState.value = _uiState.value.copy(collectionMenus = data)
    }

    fun getImageLoader(): ImageLoader = imageLoader

    fun updateCollectionSelected(itemSelected: CollectionMenu) {
        if (itemSelected.id == _uiState.value.selectedCollectionId) return
        _uiState.value = _uiState.value.copy(selectedCollectionId = itemSelected.id)

    }
}