package com.kyobi.domain.provider.auth

import com.kyobi.domain.model.LoggedInUser

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false,
    val isAnonymous: Boolean = false,
    val currentUser: LoggedInUser? = null
)