package com.kyobi.domain.usecase.impl

import com.kyobi.domain.repository.AppConfigRepository
import com.kyobi.domain.usecase.AddRemoveProductToFavoriteUseCase
import com.kyobi.domain.usecase.AppVersionUseCase
import javax.inject.Inject

class AddRemoveProductToFavoriteUseCaseImpl @Inject constructor(
    private val appConfigRepository: AppConfigRepository
): AddRemoveProductToFavoriteUseCase {
    override suspend fun addOrRemoveProductToFavourite() {
        TODO("Not yet implemented")
    }

}