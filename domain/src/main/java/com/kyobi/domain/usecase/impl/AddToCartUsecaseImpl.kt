package com.kyobi.domain.usecase.impl

import com.kyobi.domain.repository.AppConfigRepository
import com.kyobi.domain.usecase.AddToCartUseCase
import javax.inject.Inject

class AddToCartUseCaseImpl @Inject constructor(
    private val appConfigRepository: AppConfigRepository
): AddToCartUseCase {
    override suspend fun addItemToCart() {
        TODO("Not yet implemented")
    }
}