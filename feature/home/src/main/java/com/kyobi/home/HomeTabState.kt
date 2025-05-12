package com.kyobi.home

import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.Product
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.model.TrendingResearch

data class HomeTabUiState(
    val recommendedProductsResult: DomainNetworkResult<List<Product>> = DomainNetworkResult.Success(emptyList()),
    val bannersResult: DomainNetworkResult<List<Banner>> = DomainNetworkResult.Success(emptyList()),
    val topCatalogsResult: DomainNetworkResult<List<TopCatalog>> = DomainNetworkResult.Success(emptyList()),
    val trendingResearch: DomainNetworkResult<List<TrendingResearch>> = DomainNetworkResult.Success(emptyList()),

)