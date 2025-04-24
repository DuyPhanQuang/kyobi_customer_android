package com.kyobi.core.exceptions

class KyobiApiException(message: String, val code: Int? = null, cause: Throwable? = null
) : Exception(message, cause)