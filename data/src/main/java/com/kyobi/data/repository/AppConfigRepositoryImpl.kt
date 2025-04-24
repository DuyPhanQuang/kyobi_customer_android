package com.kyobi.data.repository

import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.AppVersion
import com.kyobi.domain.repository.AppConfigRepository
import javax.inject.Inject

class AppConfigRepositoryImpl @Inject constructor(
    private val apiService: KyobiApiService,
): AppConfigRepository {
    override suspend fun getAppVersion(): AppVersion {
        val response = apiService.getAppVersion()
        return response.toAppVersion()
    }

}