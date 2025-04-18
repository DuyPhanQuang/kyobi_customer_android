package com.kyobi.domain.usecase.impl

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.LoggedInUser
import com.kyobi.domain.model.request.LoginRequest
import com.kyobi.domain.repository.AuthRepository
import com.kyobi.domain.usecase.LoginUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class LoginUsecaseImpl @Inject constructor(
    private val authRepository: AuthRepository
): LoginUseCase {
    override suspend fun login(email: String, password: String): Flow<DomainNetworkResult<LoggedInUser>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            val request = LoginRequest(email = email, password = password)
            when (val result = authRepository.login(request)) {
                is RestNetworkResult.Success -> emit(DomainNetworkResult.Success(result.data))
                is RestNetworkResult.Error -> emit(DomainNetworkResult.Error(Throwable(result.message)))
                is RestNetworkResult.Loading -> emit(DomainNetworkResult.Loading)
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error(throwable))
        }
    }

    override suspend fun loginAnonymously(): Flow<DomainNetworkResult<LoggedInUser>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            when (val result = authRepository.loginAnonymously()) {
                is RestNetworkResult.Success -> emit(DomainNetworkResult.Success(result.data))
                is RestNetworkResult.Error -> emit(DomainNetworkResult.Error(Throwable(result.message)))
                is RestNetworkResult.Loading -> emit(DomainNetworkResult.Loading)
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error(throwable))
        }
    }

    override suspend fun getCurrentUser(): Flow<DomainNetworkResult<LoggedInUser>> {
        return flow {
            when (val result = authRepository.getAuthUser()) {
                is RestNetworkResult.Success -> emit(DomainNetworkResult.Success(result.data))
                is RestNetworkResult.Error -> emit(DomainNetworkResult.Error(Throwable(result.message)))
                is RestNetworkResult.Loading -> emit(DomainNetworkResult.Loading)
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error(throwable))
        }
    }

}