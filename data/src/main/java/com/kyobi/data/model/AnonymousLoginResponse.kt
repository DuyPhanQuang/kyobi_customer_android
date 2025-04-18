package com.kyobi.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AnonymousLoginResponse(
    val message: String,
    val data: AnonymousLoginData
)

@JsonClass(generateAdapter = true)
data class AnonymousLoginData(
    val user: AnonymousUser,
    val session: AnonymousSession
)

@JsonClass(generateAdapter = true)
data class AnonymousUser(
    val id: String,
    val aud: String,
    val role: String,
    val email: String,
    val phone: String,
    @Json(name = "last_sign_in_at") val lastSignInAt: String,
    @Json(name = "app_metadata") val appMetadata: Map<String, Any> = emptyMap(),
    @Json(name = "user_metadata") val userMetadata: Map<String, Any> = emptyMap(),
    val identities: List<Any> = emptyList(),
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "is_anonymous") val isAnonymous: Boolean
)

@JsonClass(generateAdapter = true)
data class AnonymousSession(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "expires_in") val expiresIn: Int,
    @Json(name = "expires_at") val expiresAt: Long,
    @Json(name = "refresh_token") val refreshToken: String,
    val user: AnonymousUser
)