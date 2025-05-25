package com.kyobi.feature.collection.screen.tab

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.ImageLoader
import com.kyobi.core.coroutines.launchOnIO
import com.kyobi.domain.model.CategoryMenu
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.SubcategoryMenu
import com.kyobi.domain.usecase.GetSubMenusUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class CollectionTabViewModel @Inject constructor(
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

    fun onRefreshTriggered(onCompleted: () -> Unit) {
        processingRefreshSubMenusIfNeeded()
        processingRefreshProducts()
        onCompleted()
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

    private fun processingRefreshSubMenusIfNeeded() {
        var shouldRefreshSubMenus = false
        when (val result = _uiState.value.subMenusResult) {
            is DomainNetworkResult.Error -> shouldRefreshSubMenus = true
            is DomainNetworkResult.Loading -> shouldRefreshSubMenus = true
            is DomainNetworkResult.Success -> {
                val menus = result.data
                if (menus.isEmpty()) {
                    shouldRefreshSubMenus = true
                }
            }
        }
        if (shouldRefreshSubMenus) {
            fetchSubMenus()
        }
    }

    private fun processingRefreshProducts() {
        val currentSubCategory = _uiState.value.selectedSubCategory
        val currentCategory = _uiState.value.selectedCategory
        var finalHandle: String? = null
        if (currentSubCategory != null) {
            finalHandle = currentSubCategory.filterHandle
        } else if (currentCategory != null) {
            finalHandle = currentCategory.filterHandle
        }
        requestAfterRefreshTriggered(finalHandle)
    }

    private fun fetchSubMenus() {
        val collectionHandle = "women"
        viewModelScope.launchOnIO {
            getSubMenusUseCase.getSubMenus(handle = collectionHandle).collect { result ->
                _uiState.value = _uiState.value.copy(subMenusResult = result)
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
        requestAfterCategorySelected(category)
    }

    fun updateSubCategorySelected(subCategory: SubcategoryMenu, categoryMenus: List<CategoryMenu>) {
        if (subCategory.id == _uiState.value.selectedSubCategoryId) {
            _uiState.value = _uiState.value.copy(
                selectedSubCategory = null,
                selectedSubCategoryId = null
            )
            val currentCategory = _uiState.value.selectedCategory!!
            requestAfterCategorySelected(currentCategory)
        } else {
            val parentCategory = categoryMenus.find { category ->
                category.groups?.any { group ->
                    group.subcategories?.any { sub -> sub.id == subCategory.id } == true
                } == true
            }
            if (parentCategory == null) throw Exception()
            _uiState.value = _uiState.value.copy(
                selectedCategory = parentCategory,
                selectedCategoryId = parentCategory.id,
                selectedSubCategory = subCategory,
                selectedSubCategoryId = subCategory.id
            )
            requestAfterSubCategorySelected(subCategory)
        }
    }

    private fun requestAfterCategorySelected(category: CategoryMenu) {
        viewModelScope.launch {
            collectionTabEventBus.emitEvent(CollectionTabEvent.CategorySelected(category.filterHandle))
            Timber.tag(tag).d("Emitted CategorySelected event with filterHandle: ${category.filterHandle}")
        }
    }

    private fun requestAfterSubCategorySelected(subCategory: SubcategoryMenu) {
        viewModelScope.launch {
            collectionTabEventBus.emitEvent(CollectionTabEvent.SubCategorySelected(subCategory.filterHandle))
            Timber.tag(tag).d("Emitted SubCategorySelected event with filterHandle: ${subCategory.filterHandle}")
        }
    }

    private fun requestAfterRefreshTriggered(filterHandle: String?) {
        viewModelScope.launch {
            collectionTabEventBus.emitEvent(CollectionTabEvent.RefreshTriggered(filterHandle))
            Timber.tag(tag).d("Emitted RefreshTriggered event with filterHandle: $filterHandle")
        }
    }
}