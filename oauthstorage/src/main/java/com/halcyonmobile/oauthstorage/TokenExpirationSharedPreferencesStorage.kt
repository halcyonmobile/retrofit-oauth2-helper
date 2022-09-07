package com.halcyonmobile.oauthstorage

import android.content.SharedPreferences
import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage

class TokenExpirationSharedPreferencesStorage(private val sharedPreferences: SharedPreferences) : TokenExpirationStorage {
    override var accessTokenExpiresAt: Long
        get() = sharedPreferences.getLong(EXPIRATION_TOKEN, Long.MIN_VALUE)
        set(value) = sharedPreferences.edit().putLong(EXPIRATION_TOKEN, value).apply()

    override fun clear() {
        sharedPreferences.edit()
            .remove(EXPIRATION_TOKEN)
            .apply()
    }

    companion object {
        private const val EXPIRATION_TOKEN = "accessTokenExpiresAt"
    }
}
