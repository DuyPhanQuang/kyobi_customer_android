package com.kyobi.domain.repository

import com.kyobi.domain.model.LoggedInUser
import com.kyobi.domain.model.request.LoginRequest
import com.kyobi.domain.model.request.SignupRequest

interface AuthRepository {
    suspend fun login(request: LoginRequest): LoggedInUser
    suspend fun loginAnonymously(): LoggedInUser
    suspend fun getAuthUser(): LoggedInUser
    suspend fun logout()
    suspend fun signup(request: SignupRequest): Boolean
}