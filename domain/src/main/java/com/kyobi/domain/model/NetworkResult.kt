package com.kyobi.domain.model

import com.kyobi.core.exceptions.KyobiApiException

sealed class DomainNetworkResult<out T> {
    data object Loading : DomainNetworkResult<Nothing>()
    data class Success<out T>(val data: T) : DomainNetworkResult<T>()
    sealed class Error : DomainNetworkResult<Nothing>() {
        data class KyobiApi(val exception: KyobiApiException) : Error()
        data class Generic(val throwable: Throwable) : Error()
    }
}