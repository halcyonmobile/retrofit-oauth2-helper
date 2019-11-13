/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthstorage

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.CallSuper
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage

/**
 * [SharedPreferences] based implementation of [AuthenticationLocalStorage].
 *
 * Can be used as a separate SharedPreferencesManager just for session, or added to your custom one the custom one.
 * Because all functions are defined in [AuthenticationLocalStorage], you may use this as delegate for your custom
 * SharedPreferencesManager.
 */
open class AuthenticationSharedPreferencesStorage(val sharedPreferences: SharedPreferences) : AuthenticationLocalStorage {

    constructor(context: Context, key: String = PREFERENCES_KEY): this(createSharedPreferences(context, key))

    final override var userId: String
        get() = sharedPreferences.getString(USER_ID, "").orEmpty()
        set(value) = sharedPreferences.putString(USER_ID, value)

    final override var accessToken: String
        get() = sharedPreferences.getString(AUTH_TOKEN, "").orEmpty()
        set(value) = sharedPreferences.putString(AUTH_TOKEN, value)

    final override var tokenType: String
        get() = sharedPreferences.getString(AUTH_TOKEN_TYPE, "").orEmpty()
        set(value) = sharedPreferences.putString(AUTH_TOKEN_TYPE, value)

    final override var refreshToken: String
        get() = sharedPreferences.getString(REFRESH_TOKEN, "").orEmpty()
        set(value) = sharedPreferences.putString(REFRESH_TOKEN, value)


    @CallSuper
    override fun clear() {
        sharedPreferences.edit()
            .remove(AUTH_TOKEN)
            .remove(REFRESH_TOKEN)
            .remove(AUTH_TOKEN_TYPE)
            .remove(USER_ID)
            .apply()
    }

    companion object {
        const val PREFERENCES_KEY = "preferences"
        private const val AUTH_TOKEN = "authentication_token"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val AUTH_TOKEN_TYPE = "auth_token_type"
        private const val USER_ID = "user_id"

        private fun SharedPreferences.putString(key: String, value: String) =
            edit().putString(key, value).apply()

        fun create(context: Context, key: String = PREFERENCES_KEY): AuthenticationSharedPreferencesStorage =
            AuthenticationSharedPreferencesStorage(context, key)

        fun createSharedPreferences(context: Context, key: String): SharedPreferences =
            context.getSharedPreferences(key, Context.MODE_PRIVATE)
    }
}