package com.kyobi.domain.usecase

interface AddRemoveProductToFavoriteUseCase {
    suspend fun addOrRemoveProductToFavourite()
}