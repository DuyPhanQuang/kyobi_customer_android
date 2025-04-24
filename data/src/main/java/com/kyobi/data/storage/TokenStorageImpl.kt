package com.kyobi.data.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.kyobi.core.storage.TokenStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

const val SHARED_PREFS_FILENAME = "kyobi_encrypted_prefs"
const val ACCESS_TOKEN_KEY = "access_token"
const val REFRESH_TOKEN_KEY = "refresh_token"
const val FCM_TOKEN_KEY = "fcm_token"

@Singleton
class TokenStorageImpl @Inject constructor(
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

    override fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit {
            putString(ACCESS_TOKEN_KEY, accessToken)
                .putString(REFRESH_TOKEN_KEY, refreshToken)
        }
    }

    override fun getAccessToken(): String? {
        return prefs.getString(ACCESS_TOKEN_KEY, null)
    }

    override fun getRefreshToken(): String? {
        return prefs.getString(REFRESH_TOKEN_KEY, null)
    }

    override fun clearTokens() {
        prefs.edit {
            remove(ACCESS_TOKEN_KEY)
                .remove(REFRESH_TOKEN_KEY)
        }
    }

    override fun saveFcmToken(fcmToken: String) {
        prefs.edit {
            putString(ACCESS_TOKEN_KEY, fcmToken)
        }
    }

    override fun clearFcmToken() {
        prefs.edit {
            remove(FCM_TOKEN_KEY)
        }
    }

    override fun getFcmToken(): String? {
        return prefs.getString(FCM_TOKEN_KEY, null)
    }
}