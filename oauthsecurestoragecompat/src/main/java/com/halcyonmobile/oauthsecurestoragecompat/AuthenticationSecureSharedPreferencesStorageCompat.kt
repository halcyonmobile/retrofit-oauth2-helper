/*
 * Copyright (c) 2020 Halcyon Mobile.
 * https://www.halcyonmobile.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.halcyonmobile.oauthsecurestoragecompat

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauthsecurestorage.AuthenticationSecureSharedPreferencesStorage
import com.halcyonmobile.oauthstorage.AuthenticationSharedPreferencesStorage

/**
 * Implementation of [AuthenticationLocalStorage] if possible uses [AuthenticationSecureSharedPreferencesStorage] (api version above 23) or fallbacks to
 * [AuthenticationSharedPreferencesStorage] otherwise.
 *
 * Note: to make sure if the user upgrades their api lvl a migration has been introduced.
 * This can also be used if you were using [AuthenticationSharedPreferencesStorage] previously and want to update to [AuthenticationSecureSharedPreferencesStorage].
 */
open class AuthenticationSecureSharedPreferencesStorageCompat(private val authenticationLocalStorage: AuthenticationSharedPreferencesStorage) :
    AuthenticationLocalStorage by authenticationLocalStorage {

    val sharedPreferences get() = authenticationLocalStorage.sharedPreferences

    constructor(
        context: Context,
        encryptedFileName: String = AuthenticationSecureSharedPreferencesStorage.ENCRYPTED_FILE_NAME,
        preferenceKey: String = AuthenticationSharedPreferencesStorage.PREFERENCES_KEY
    ) : this(createAndMigrate(context, encryptedFileName, preferenceKey))

    companion object {

        fun createAndMigrate(
            context: Context,
            encryptedFileName: String,
            preferenceKey: String
        ): AuthenticationSharedPreferencesStorage {
            val nonEncryptedSharedPreferences = AuthenticationSharedPreferencesStorage.create(context, preferenceKey)

            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val authenticationSecureSharedPreferencesStorage = AuthenticationSecureSharedPreferencesStorage.create(context, encryptedFileName)
                migrateIfNeeded(nonEncryptedSharedPreferences, authenticationSecureSharedPreferencesStorage)

                authenticationSecureSharedPreferencesStorage
            } else {
                nonEncryptedSharedPreferences
            }
        }

        private fun migrateIfNeeded(
            nonEncryptedAuthenticationLocalStorage: AuthenticationSharedPreferencesStorage,
            encryptedAuthenticationLocalStorage: AuthenticationSecureSharedPreferencesStorage
        ) {
            if (nonEncryptedAuthenticationLocalStorage.sharedPreferences.all.isNotEmpty()) {
                nonEncryptedAuthenticationLocalStorage.sharedPreferences.copyTo(encryptedAuthenticationLocalStorage.sharedPreferences)

                nonEncryptedAuthenticationLocalStorage.sharedPreferences.edit().clear().apply()
            }
        }

        @SuppressLint("CommitPrefEdits")
        fun SharedPreferences.copyTo(sharedPreferences: SharedPreferences) {
            all.asSequence().fold(sharedPreferences.edit()) { editor, (key, value) ->
                when(value){
                    is Long -> editor.putLong(key, value)
                    is Boolean -> editor.putBoolean(key, value)
                    is Float -> editor.putFloat(key, value)
                    is Int -> editor.putInt(key, value)
                    is String -> editor.putString(key, value)
                    is Set<*> -> editor.putStringSet(key, value.filterIsInstance<String>().toSet())
                    else -> editor
                }
            }
                .apply()
        }
    }
}