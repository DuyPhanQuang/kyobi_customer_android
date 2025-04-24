package com.kyobi.domain.repository

import com.kyobi.domain.model.AppVersion

interface AppConfigRepository {
    suspend fun getAppVersion(): AppVersion
}