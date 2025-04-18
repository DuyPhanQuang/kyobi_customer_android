package com.kyobi.home

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product

data class HomeTabUiState(
    val productsResult: DomainNetworkResult<List<Product>> = DomainNetworkResult.Success(emptyList()),
)