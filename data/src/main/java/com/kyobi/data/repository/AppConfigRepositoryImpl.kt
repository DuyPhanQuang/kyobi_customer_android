package com.kyobi.data.repository

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.core.exceptions.KyobiApiException
import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.AppVersion
import com.kyobi.domain.repository.AppConfigRepository
import javax.inject.Inject

class AppConfigRepositoryImpl @Inject constructor(
    private val apiService: KyobiApiService,
): AppConfigRepository {
    override suspend fun getAppVersion(): RestNetworkResult<AppVersion> {
        return try {
            val response = apiService.getAppVersion()
            RestNetworkResult.Success(response.toAppVersion())
        } catch (e: KyobiApiException) {
            RestNetworkResult.Error(e.message ?: "Unknown error", e.code)
        }
    }

}