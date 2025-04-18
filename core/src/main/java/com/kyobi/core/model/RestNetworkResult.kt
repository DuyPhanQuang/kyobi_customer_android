package com.kyobi.core.model

sealed class RestNetworkResult<out T> {
    data class Success<out T>(val data: T) : RestNetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : RestNetworkResult<Nothing>()
    data object Loading : RestNetworkResult<Nothing>()
}