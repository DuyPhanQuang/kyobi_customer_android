package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.domain.model.Banner
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.TopCatalog
import com.kyobi.domain.model.TrendingResearch
import com.kyobi.domain.repository.PageRepository
import com.kyobi.domain.usecase.GetHomePagesUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetHomePagesUseCaseImpl @Inject constructor(
    private val pageRepository: PageRepository
) : GetHomePagesUseCase {
    override suspend fun getHomeBanners(): Flow<DomainNetworkResult<List<Banner>>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val result = pageRepository.getBannersFromShopify(
                    handle = "homepage",
                    key = "homepage_banner"
                )
                emit(DomainNetworkResult.Success(result))
            } catch (e: ShopifyApiException) {
                emit(DomainNetworkResult.Error.ShopifyApi(e))
            } catch (e: Exception) {
                emit(DomainNetworkResult.Error.Generic(e))
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error.Generic(throwable))
        }
    }

    override suspend fun getHomeTopCatalogs(): Flow<DomainNetworkResult<List<TopCatalog>>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val result = pageRepository.getTopCatalogsFromShopify(
                    handle = "homepage",
                    key = "homepage_top_catalog"
                )
                emit(DomainNetworkResult.Success(result))
            } catch (e: ShopifyApiException) {
                emit(DomainNetworkResult.Error.ShopifyApi(e))
            } catch (e: Exception) {
                emit(DomainNetworkResult.Error.Generic(e))
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error.Generic(throwable))
        }
    }

    override suspend fun getHomeTrendingResearchs(): Flow<DomainNetworkResult<List<TrendingResearch>>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val result = pageRepository.getTrendingResearchsFromShopify(
                    handle = "homepage",
                    key = "homepage_trending"
                )
                emit(DomainNetworkResult.Success(result))
            } catch (e: ShopifyApiException) {
                emit(DomainNetworkResult.Error.ShopifyApi(e))
            } catch (e: Exception) {
                emit(DomainNetworkResult.Error.Generic(e))
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error.Generic(throwable))
        }
    }
}