package com.kyobi.core.responses

import com.google.gson.annotations.SerializedName

data class ServerErrorResponse(
    @SerializedName("code") val code: Int? = 0,
    @SerializedName("message") val message: String? = "",
)