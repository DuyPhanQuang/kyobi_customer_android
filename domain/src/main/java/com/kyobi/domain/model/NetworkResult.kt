package com.kyobi.domain.model

sealed class DomainNetworkResult<out T> {
    data class Success<out T>(val data: T) : DomainNetworkResult<T>()
    data class Error(val exception: Throwable) : DomainNetworkResult<Nothing>()

    data object Loading : DomainNetworkResult<Nothing>()
}