package com.kyobi.domain.usecase.impl

import com.kyobi.core.exceptions.KyobiApiException
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
    override suspend operator fun invoke(email: String, password: String): Flow<DomainNetworkResult<LoggedInUser>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            val request = LoginRequest(email = email, password = password)
            try {
                val result = authRepository.login(request)
                emit(DomainNetworkResult.Success(result))
            } catch (e: KyobiApiException) {
                emit(DomainNetworkResult.Error.KyobiApi(e))
            } catch (e: Exception) {
                emit(DomainNetworkResult.Error.Generic(e))
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error.Generic(throwable))
        }
    }

    override suspend fun loginAnonymously(): Flow<DomainNetworkResult<LoggedInUser>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            try {
                val result = authRepository.loginAnonymously()
                emit(DomainNetworkResult.Success(result))
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