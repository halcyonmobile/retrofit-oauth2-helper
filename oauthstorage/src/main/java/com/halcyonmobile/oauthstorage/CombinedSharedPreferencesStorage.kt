package com.halcyonmobile.oauthstorage

import android.content.Context
import android.content.SharedPreferences
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage

class CombinedSharedPreferencesStorage(
    private val authenticationLocalStorage: AuthenticationLocalStorage,
    private val tokenExpirationStorage: TokenExpirationStorage,
) : AuthenticationLocalStorage by authenticationLocalStorage,
    TokenExpirationStorage by tokenExpirationStorage {

    override fun clear() {
        authenticationLocalStorage.clear()
        tokenExpirationStorage.clear()
    }

    companion object {

        fun create(context: Context, key: String = AuthenticationSharedPreferencesStorage.PREFERENCES_KEY): CombinedSharedPreferencesStorage =
            create(sharedPreferences = AuthenticationSharedPreferencesStorage.createSharedPreferences(context, key))

        fun create(sharedPreferences: SharedPreferences): CombinedSharedPreferencesStorage {
            val authenticationLocalStorage = AuthenticationSharedPreferencesStorage(sharedPreferences)
            val tokenExpirationStorage = TokenExpirationSharedPreferencesStorage(sharedPreferences)

            return CombinedSharedPreferencesStorage(
                authenticationLocalStorage = authenticationLocalStorage,
                tokenExpirationStorage = tokenExpirationStorage
            )
        }
    }
}
