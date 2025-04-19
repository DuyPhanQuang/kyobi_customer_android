package com.kyobi.domain.usecase.impl

import com.kyobi.core.model.RestNetworkResult
import com.kyobi.domain.model.DomainNetworkResult
import com.kyobi.domain.model.request.SignupRequest
import com.kyobi.domain.repository.AuthRepository
import com.kyobi.domain.usecase.SignUpUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class SignupUsecaseImpl @Inject constructor(
    private val authRepository: AuthRepository
): SignUpUseCase {
    override suspend fun signUp(
        email: String,
        password: String,
        phone: String?
    ): Flow<DomainNetworkResult<Boolean>> {
        return flow {
            emit(DomainNetworkResult.Loading)
            val request = SignupRequest(
                email = email,
                password = password,
                phone = phone)
            when (val result = authRepository.signup(request)) {
                is RestNetworkResult.Success -> emit(DomainNetworkResult.Success(result.data))
                is RestNetworkResult.Error -> emit(DomainNetworkResult.Error(Throwable(result.message)))
                is RestNetworkResult.Loading -> emit(DomainNetworkResult.Loading)
            }
        }.catch { throwable ->
            emit(DomainNetworkResult.Error(throwable))
        }
    }

}