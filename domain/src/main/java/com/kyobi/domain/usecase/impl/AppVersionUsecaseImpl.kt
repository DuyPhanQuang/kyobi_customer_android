package com.kyobi.domain.usecase.impl

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.domain.model.AppVersion
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.repository.AppConfigRepository
import com.kyobi.domain.usecase.AppVersionUsecase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class AppVersionUsecaseImpl @Inject constructor(
    private val appConfigRepository: AppConfigRepository
): AppVersionUsecase {
    override suspend fun getAppVersion(): Flow<DomainNetworkResult<AppVersion>> {
        return flow {
            when (val result = appConfigRepository.getAppVersion()) {
                is RestNetworkResult.Success -> emit(DomainNetworkResult.Success(result.data))
                is RestNetworkResult.Error -> emit(DomainNetworkResult.Error(Throwable(result.message)))
                is RestNetworkResult.Loading -> emit(DomainNetworkResult.Loading)
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error(throwable))
        }
    }

}