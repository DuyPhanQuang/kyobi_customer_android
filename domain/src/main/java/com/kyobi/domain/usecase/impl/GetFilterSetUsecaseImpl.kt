package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.domain.model.CateFilter
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.repository.MetaobjectRepository
import com.kyobi.domain.usecase.GetFilterSetUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetFilterSetUseCaseImpl @Inject constructor(
    private val metaobjectRepository: MetaobjectRepository
): GetFilterSetUseCase {
    override suspend fun getFilterSetByCateHandle(
        handle: String
    ): Flow<DomainNetworkResult<CateFilter?>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val cateFilter = metaobjectRepository.getFilterSetByCateHandle(handle)
                emit(DomainNetworkResult.Success(cateFilter))
            } catch (e: ShopifyApiException) {
                emit(DomainNetworkResult.Error.ShopifyApi(e))
            } catch (e: Exception) {
                emit(DomainNetworkResult.Error.Generic(e))
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error.Generic(throwable))
        }
    }
    override suspend fun getFilterSetByDefault(): Flow<DomainNetworkResult<CateFilter?>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val cateFilter = metaobjectRepository.getFilterSetByCateHandle("women")
                emit(DomainNetworkResult.Success(cateFilter))
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