package com.kyobi.feature.collection.screen.tab

import com.kyobi.domain.model.CategoryMenu
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.SubcategoryMenu

data class CollectionTabUiState(
    val subMenusResult: DomainNetworkResult<List<CategoryMenu>> = DomainNetworkResult.Success(emptyList()),
    val selectedCategoryId: String? = null,
    val selectedSubCategoryId: String? = null,
    val selectedCategory: CategoryMenu? = null,
    val selectedSubCategory: SubcategoryMenu? = null
)