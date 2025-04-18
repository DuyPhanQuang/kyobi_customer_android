package com.kyobi.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val user: User,
    val session: Session
)

@JsonClass(generateAdapter = true)
data class Session(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int,
    @Json(name = "expires_at") val expiresAt: Long,
    @Json(name = "refresh_token") val refreshToken: String,
    val user: User
)

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val aud: String,
    val role: String,
    val email: String,
    @Json(name = "email_confirmed_at") val emailConfirmedAt: String?,
    val phone: String?,
    @Json(name = "confirmation_sent_at") val confirmationSentAt: String?,
    @Json(name = "confirmed_at") val confirmedAt: String?,
    @Json(name = "last_sign_in_at") val lastSignInAt: String?,
    @Json(name = "app_metadata") val appMetadata: AppMetadata?,
    @Json(name = "user_metadata") val userMetadata: UserMetadata?,
    val identities: List<Identity>?,
    @Json(name = "created_at") val createdAt: String?,
    @Json(name = "updated_at") val updatedAt: String?,
    @Json(name = "is_anonymous") val isAnonymous: Boolean
)