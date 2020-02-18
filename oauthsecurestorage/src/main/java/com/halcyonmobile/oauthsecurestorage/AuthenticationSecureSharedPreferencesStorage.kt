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
package com.halcyonmobile.oauthsecurestorage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauthstorage.AuthenticationSharedPreferencesStorage


/**
 * [EncryptedSharedPreferences] based implementation of [AuthenticationLocalStorage].
 *
 * Can be used as a separate SharedPreferencesManager just for session, or added to your custom one.
 * Because all functions are defined in [AuthenticationLocalStorage], you may use this as delegate for your custom
 * SharedPreferencesManager.
 */
open class AuthenticationSecureSharedPreferencesStorage(sharedPreferences: EncryptedSharedPreferences) : AuthenticationSharedPreferencesStorage(sharedPreferences) {

    constructor(context: Context, encryptedFileName: String = ENCRYPTED_FILE_NAME) : this(
        createEncryptedSharedPreferences(context, encryptedFileName)
    )

    companion object {
        const val ENCRYPTED_FILE_NAME = "encrypted_preferences"

        fun create(context: Context, encryptedFileName: String = ENCRYPTED_FILE_NAME) : AuthenticationSecureSharedPreferencesStorage =
            AuthenticationSecureSharedPreferencesStorage(context, encryptedFileName)

        fun createEncryptedSharedPreferences(context: Context, encryptedFileName: String = ENCRYPTED_FILE_NAME): EncryptedSharedPreferences {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

            return EncryptedSharedPreferences.create(
                encryptedFileName,
                masterKeyAlias,
                context,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            ) as EncryptedSharedPreferences
        }
    }
}