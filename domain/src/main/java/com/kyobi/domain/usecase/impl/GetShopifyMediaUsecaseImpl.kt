package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.ShopifyApiException
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.ShopifyMedia
import com.kyobi.domain.repository.MediaRepository
import com.kyobi.domain.usecase.GetShopifyMediaUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetShopifyMediaUseCaseImpl @Inject constructor(
    private val mediaRepository: MediaRepository
): GetShopifyMediaUseCase {
    override suspend fun getImagesByIds(
        imagesIds: List<String>
    ): Flow<DomainNetworkResult<List<ShopifyMedia>>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val result = mediaRepository.getMediaImagesByIdsFromShopify(imagesIds)
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