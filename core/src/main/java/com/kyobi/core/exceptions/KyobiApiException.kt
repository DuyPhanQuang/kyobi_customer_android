package com.kyobi.core.exceptions

class KyobiApiException(message: String, val code: Int? = null) : Exception(message)