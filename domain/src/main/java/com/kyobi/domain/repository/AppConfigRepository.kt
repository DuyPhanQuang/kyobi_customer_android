package com.kyobi.domain.repository

import com.kyobi.domain.model.AppVersion
import com.kyobi.domain.model.AssetSource
import com.kyobi.domain.model.Assets

interface AppConfigRepository {
    suspend fun getAppVersion(): AppVersion
    suspend fun getAssetSource(): AssetSource
    suspend fun getAssets(
        query: String?,
        page: Int?,
        perPage: Int?,
        locale: String?
    ): Assets
}