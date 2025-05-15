package com.kyobi.domain.usecase

import com.kyobi.domain.model.CategoryMenu
import com.kyobi.domain.model.DomainNetworkResult
import kotlinx.coroutines.flow.Flow

interface GetSubMenusUseCase {
    suspend fun getSubMenus(handle: String): Flow<DomainNetworkResult<List<CategoryMenu>>>
}