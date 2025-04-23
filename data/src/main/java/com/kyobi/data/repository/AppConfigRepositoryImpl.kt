package com.kyobi.data.repository

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.AppVersion
import com.kyobi.domain.repository.AppConfigRepository
import javax.inject.Inject

class AppConfigRepositoryImpl @Inject constructor(
    private val apiService: KyobiApiService,
): AppConfigRepository {
    override suspend fun getAppVersion(): RestNetworkResult<AppVersion> {
        return when (val result = apiService.getAppVersion()) {
            is RestNetworkResult.Success -> RestNetworkResult.Success(result.data.toAppVersion())
            is RestNetworkResult.Error -> RestNetworkResult.Error(result.message, result.code)
            is RestNetworkResult.Loading -> RestNetworkResult.Loading
        }
    }

}