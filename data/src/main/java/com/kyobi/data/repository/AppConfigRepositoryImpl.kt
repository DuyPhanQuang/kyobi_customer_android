package com.kyobi.data.repository

import com.kyobi.data.network.KyobiApiService
import com.kyobi.domain.model.AppVersion
import com.kyobi.domain.model.AssetSource
import com.kyobi.domain.model.Assets
import com.kyobi.domain.repository.AppConfigRepository
import javax.inject.Inject

class AppConfigRepositoryImpl @Inject constructor(
    private val apiService: KyobiApiService,
): AppConfigRepository {
    override suspend fun getAppVersion(): AppVersion {
        val response = apiService.getAppVersion()
        return response.toAppVersion()
    }

    override suspend fun getAssetSource(): AssetSource {
        val response = apiService.getAssetSource()
        return response.toAssetSource()
    }

    override suspend fun getAssets(
        query: String?,
        page: Int?,
        perPage: Int?,
        locale: String?
    ): Assets {
        val response = apiService.getAssets(query, page, perPage, locale)
        return response.toAssets()
    }
}