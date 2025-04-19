package com.kyobi.domain.usecase.impl

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.repository.AuthRepository
import com.kyobi.domain.usecase.LogoutUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LogoutUsecaseImpl @Inject constructor(
    private val authRepository: AuthRepository,
): LogoutUseCase {
    override fun logout(): Flow<DomainNetworkResult<Unit>> {
        return flow {
            when (val result = authRepository.logout()) {
                is RestNetworkResult.Success -> emit(DomainNetworkResult.Success(Unit))
                is RestNetworkResult.Error -> emit(DomainNetworkResult.Error(Throwable(result.message)))
                is RestNetworkResult.Loading -> {}
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error(throwable))
        }
    }

}