package com.kyobi.feature.collection.screen.tab

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.CategoryMenu
import com.kyobi.domain.model.DomainNetworkResult
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
    private val collectionTabEventBus: CollectionTabEventBus
): ViewModel() {
    private val tag = "CollectionTabViewModel"
    private val _uiState = MutableStateFlow(CollectionTabUiState())
    val uiState = _uiState.asStateFlow()

    init {
        fetchSubMenus()
    }

    fun getCollectionTabEventBus(): CollectionTabEventBus = collectionTabEventBus

    fun getImageLoader(): ImageLoader = imageLoader

    fun getCategorySelected(categoryId: String?): CategoryMenu? {
        if (categoryId == null) return null
        val itemSelected = _uiState.value.selectedCategory ?: return null
        if (categoryId == itemSelected.id) return itemSelected
        return null
    }

    fun getSubCategorySelected(subCategoryId: String?): SubcategoryMenu? {
        if (subCategoryId == null) return null
        val itemSelected = _uiState.value.selectedSubCategory ?: return null
        if (subCategoryId == itemSelected.id) return itemSelected
        return null
    }

    fun getCategories(): List<CategoryMenu>? {
        val data = when (val result = _uiState.value.subMenusResult) {
            is DomainNetworkResult.Success -> result.data
            is DomainNetworkResult.Loading -> null
            is DomainNetworkResult.Error -> null
        }
        return data
    }

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
        if (category.id == _uiState.value.selectedCategoryId) return
        _uiState.value = _uiState.value.copy(
            selectedCategory = category,
            selectedCategoryId = category.id,
            selectedSubCategory = null,
            selectedSubCategoryId = null
        )
        viewModelScope.launchOnIO {
            collectionTabEventBus.emitEvent(CollectionTabEvent.CategorySelected(category.filterHandle))
            Timber.tag(tag).d("Emitted CategorySelected event with filterHandle: ${category.filterHandle}")
        }
    }

    fun updateSubCategorySelected(subCategory: SubcategoryMenu, categoryMenus: List<CategoryMenu>) {
        if (subCategory.id == _uiState.value.selectedSubCategoryId) return
        val parentCategory = categoryMenus.find { category ->
            category.groups?.any { group ->
                group.subcategories?.any { sub -> sub.id == subCategory.id } == true
            } == true
        }
        if (parentCategory == null) return
        _uiState.value = _uiState.value.copy(
            selectedCategory = parentCategory,
            selectedCategoryId = parentCategory.id,
            selectedSubCategory = subCategory,
            selectedSubCategoryId = subCategory.id
        )
        viewModelScope.launchOnIO {
            collectionTabEventBus.emitEvent(CollectionTabEvent.SubCategorySelected(subCategory.filterHandle))
            Timber.tag(tag).d("Emitted SubCategorySelected event with filterHandle: ${subCategory.filterHandle}")
        }
    }
}