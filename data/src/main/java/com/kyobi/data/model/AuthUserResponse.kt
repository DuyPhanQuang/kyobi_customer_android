package com.kyobi.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AuthUserResponse(
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

@JsonClass(generateAdapter = true)
data class AppMetadata(
    val provider: String?,
    val providers: List<String>?
)

@JsonClass(generateAdapter = true)
data class UserMetadata(
    val email: String?,
    @Json(name = "email_verified") val emailVerified: Boolean?,
    @Json(name = "phone_verified") val phoneVerified: Boolean?,
    val sub: String?
)

@JsonClass(generateAdapter = true)
data class Identity(
    @Json(name = "identity_id") val identityId: String,
    val id: String,
    @Json(name = "user_id") val userId: String,
    @Json(name = "identity_data") val identityData: IdentityData,
    val provider: String,
    @Json(name = "last_sign_in_at") val lastSignInAt: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    val email: String
)

@JsonClass(generateAdapter = true)
data class IdentityData(
    val email: String?,
    @Json(name = "email_verified") val emailVerified: Boolean?,
    val phone: String?,
    @Json(name = "phone_verified") val phoneVerified: Boolean?,
    val sub: String?
)
