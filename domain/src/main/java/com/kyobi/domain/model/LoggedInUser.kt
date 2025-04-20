package com.kyobi.domain.model

data class UserInfo(
    val email: String?,
    val phoneNumber: String?,
    val nickname: String?
)

enum class UserType {
    ANONYMOUS,
    LOGGED_IN
}

data class LoggedInUser(
    val id: String,
    val userType: UserType,
    val info: UserInfo? = null
)