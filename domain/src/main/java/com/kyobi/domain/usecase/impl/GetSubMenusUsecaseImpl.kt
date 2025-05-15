package com.kyobi.domain.usecase.impl

import com.kyobi.domain.repository.CollectionRepository
import com.kyobi.domain.usecase.GetSubMenusUseCase
import javax.inject.Inject

class GetSubMenusUseCaseImpl @Inject constructor(
    private val collectionRepository: CollectionRepository,
): GetSubMenusUseCase {
}