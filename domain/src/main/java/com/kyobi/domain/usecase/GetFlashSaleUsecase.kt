package com.kyobi.domain.usecase

import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.FlashSale
import kotlinx.coroutines.flow.Flow

interface GetFlashSaleUseCase {
    suspend fun getFlashSale(handle: String): Flow<DomainNetworkResult<FlashSale>>
}