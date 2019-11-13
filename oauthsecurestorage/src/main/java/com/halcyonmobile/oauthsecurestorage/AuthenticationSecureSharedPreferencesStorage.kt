/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
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