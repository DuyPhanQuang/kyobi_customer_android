package com.kyobi.core.storage

interface TokenStorage {
    // base token
    fun saveTokens(accessToken: String, refreshToken: String)
    fun getAccessToken(): String?
    fun getRefreshToken(): String?
    fun clearTokens()

    // firebase
    fun saveFcmToken(fcmToken: String)
    fun clearFcmToken()
    fun getFcmToken(): String?
}