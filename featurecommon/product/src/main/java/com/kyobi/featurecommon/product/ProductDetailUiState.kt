package com.kyobi.featurecommon.product

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product

data class ProductDetailUiState(
    val productResult: DomainNetworkResult<Product?> = DomainNetworkResult.Success(null),
    val isFavourite: Boolean = false,
)