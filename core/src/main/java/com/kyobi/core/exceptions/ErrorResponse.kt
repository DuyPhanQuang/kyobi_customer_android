package com.kyobi.core.exceptions

import com.squareup.moshi.Json

data class ErrorResponse(
    @Json(name = "message") val message: String?  = null,
    @Json(name = "error") val error: String? = null,
    @Json(name = "statusCode") val statusCode: Int? = null
)