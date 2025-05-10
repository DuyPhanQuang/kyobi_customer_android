package com.kyobi.home

import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product

data class HomeTabUiState(
    val recommendationProductsResult: DomainNetworkResult<List<Product>> = DomainNetworkResult.Success(emptyList()),
    val bannersResult: DomainNetworkResult<List<Banner>> = DomainNetworkResult.Success(emptyList()),
)