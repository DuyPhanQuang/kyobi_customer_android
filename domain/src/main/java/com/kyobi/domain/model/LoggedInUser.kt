package com.kyobi.domain.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UserInfo(
    @Json(name = "email") val email: String?,
    @Json(name = "phone_number") val phoneNumber: String?,
    @Json(name = "nickname") val nickname: String?
)

enum class UserType {
    @Json(name = "anonymous") ANONYMOUS,
    @Json(name = "logged_in") LOGGED_IN
}

@JsonClass(generateAdapter = true)
data class LoggedInUser(
    @Json(name = "id") val id: String,
    @Json(name = "user_type") val userType: UserType,
    @Json(name = "info") val info: UserInfo? = null
)