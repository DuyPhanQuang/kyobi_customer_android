package com.kyobi.core.exceptions

import com.squareup.moshi.Json

data class ErrorResponse(
    @Json(name = "message") val message: String?,
    @Json(name = "error") val error: String?,
    @Json(name = "statusCode") val statusCode: Int?
)