package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.KyobiApiException
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
            emit(DomainNetworkResult.Loading)
            try {
                authRepository.logout()
                emit(DomainNetworkResult.Success(Unit))
            } catch (e: KyobiApiException) {
                emit(DomainNetworkResult.Error.KyobiApi(e))
            } catch (e: Exception) {
                emit(DomainNetworkResult.Error.Generic(e))
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error.Generic(throwable))
        }
    }

}