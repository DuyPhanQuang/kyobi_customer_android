package com.kyobi.feature.collection

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.CategoryMenu
import com.kyobi.domain.model.SubcategoryMenu
import com.kyobi.domain.usecase.GetSubMenusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionTabViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSubMenusUseCase: GetSubMenusUseCase,
    private val imageLoader: ImageLoader,
    private val collectionEventBus: CollectionTabEventBus
): ViewModel() {
    private val tag = "CollectionTabViewModel"
    private val _uiState = MutableStateFlow(CollectionTabUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchSubMenus()
    }

    fun getImageLoader(): ImageLoader = imageLoader

    private fun fetchSubMenus() {
        viewModelScope.launchOnIO {
            try {
                getSubMenusUseCase.getSubMenus(handle = "women").collect { result ->
                    _uiState.value = _uiState.value.copy(subMenusResult = result)
                }
            } catch (e: Exception) {
                Timber.tag(tag).e(e, "Fetch submenus failed")
            }
        }
    }

    fun updateCategorySelected(category: CategoryMenu) {
        _uiState.value = _uiState.value.copy(selectedCategoryId = category.id)
        viewModelScope.launchOnIO {
            collectionEventBus.emitCollectionTabEvent(CollectionTabEvent.CategorySelected(category.filterHandle))
            Timber.tag(tag).d("Emitted CategorySelected event with filterHandle: ${category.filterHandle}")
        }
    }

    fun updateSubCategorySelected(subCategory: SubcategoryMenu) {
        _uiState.value = _uiState.value.copy(selectedSubCategoryId = subCategory.id)
        viewModelScope.launchOnIO {
            collectionEventBus.emitCollectionTabEvent(CollectionTabEvent.SubCategorySelected(subCategory.filterHandle))
            Timber.tag(tag).d("Emitted SubCategorySelected event with filterHandle: ${subCategory.filterHandle}")
        }
    }
}