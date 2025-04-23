package com.kyobi.domain.repository

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.domain.model.AppVersion

interface AppConfigRepository {
    suspend fun getAppVersion(): RestNetworkResult<AppVersion>
}