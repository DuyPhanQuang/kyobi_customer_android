package com.kyobi.domain.usecase

import com.kyobi.domain.model.AppVersion
import com.kyobi.domain.model.DomainNetworkResult
import kotlinx.coroutines.flow.Flow

interface AppVersionUsecase {
    suspend fun getAppVersion(): Flow<DomainNetworkResult<AppVersion>>
}