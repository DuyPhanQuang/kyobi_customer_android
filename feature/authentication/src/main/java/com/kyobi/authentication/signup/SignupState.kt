package com.kyobi.authentication.signup

data class SignUpUiState(
    val email: String = "",
    val password: String = "",
    val phone: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)