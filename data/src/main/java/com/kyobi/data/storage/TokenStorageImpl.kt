package com.kyobi.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kyobi.core.storage.TokenStorage
import com.kyobi.data.database.dao.TokenDao
import com.kyobi.data.database.entity.TokenEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

const val SHARED_PREFS_FILENAME = "kyobi_encrypted_prefs"
const val ACCESS_TOKEN_KEY = "access_token"
const val REFRESH_TOKEN_KEY = "refresh_token"

@Singleton
class TokenStorageImpl @Inject constructor(
    private val tokenDao: TokenDao,
    @ApplicationContext context: Context
) : TokenStorage {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        SHARED_PREFS_FILENAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(ACCESS_TOKEN_KEY, accessToken)
            .putString(REFRESH_TOKEN_KEY, refreshToken)
            .apply()
        tokenDao.insert(
            TokenEntity(
                accessToken = accessToken,
                refreshToken = refreshToken))
    }

    override suspend fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN_KEY, null)
    }

    override suspend fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN_KEY, null)
    }

    override suspend fun clearTokens() {
        prefs.edit()
            .remove(ACCESS_TOKEN_KEY)
            .remove(REFRESH_TOKEN_KEY)
            .apply()
        tokenDao.clear()
    }
}