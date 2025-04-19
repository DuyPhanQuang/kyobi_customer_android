package com.kyobi.data.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SignupResponse(
    val message: String,
    val data: SignupData? = null
)

@JsonClass(generateAdapter = true)
data class SignupData(
    val user: SignupUser,
    val session: Any? = null
)

@JsonClass(generateAdapter = true)
data class SignupUser(
    val id: String,
    val aud: String,
    val role: String,
    val email: String,
    val phone: String,
    @Json(name = "confirmation_sent_at") val confirmationSentAt: String,
    @Json(name = "app_metadata") val appMetadata: Map<String, Any> = emptyMap(),
    @Json(name = "user_metadata") val userMetadata: UserMetadata? = null,
    val identities: List<Any> = emptyList(),
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "is_anonymous") val isAnonymous: Boolean
)
