package com.kyobi.feature.collection

import com.kyobi.domain.model.CategoryMenu
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product

data class CollectionTabUiState(
    val subMenusResult: DomainNetworkResult<List<CategoryMenu>> = DomainNetworkResult.Success(emptyList()),
    val selectedCategoryId: String? = null,
    val productsResult: DomainNetworkResult<List<Product>> = DomainNetworkResult.Success(emptyList()),
)