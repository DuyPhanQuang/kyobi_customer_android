package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.KyobiApiException
import com.kyobi.domain.model.AppVersion
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.repository.AppConfigRepository
import com.kyobi.domain.usecase.AppVersionUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AppVersionUseCaseImpl @Inject constructor(
    private val appConfigRepository: AppConfigRepository
): AppVersionUseCase {
    override suspend fun getAppVersion(): Flow<DomainNetworkResult<AppVersion>> {
        return flow {
            try {
                val result = appConfigRepository.getAppVersion()
                emit(DomainNetworkResult.Success(result))
            } catch (e: KyobiApiException) {
                emit(DomainNetworkResult.Error.KyobiApi(e))
            } catch (e: Exception) {
                emit(DomainNetworkResult.Error.Generic(e))
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error.Generic(throwable))
        }
    }

}