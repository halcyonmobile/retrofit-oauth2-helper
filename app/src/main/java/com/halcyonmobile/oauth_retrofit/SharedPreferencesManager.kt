/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth_retrofit

import android.content.Context
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauthsecurestorage.AuthenticationSecureSharedPreferencesStorage
import com.halcyonmobile.oauthsecurestoragecompat.AuthenticationSecureSharedPreferencesStorageCompat
import com.halcyonmobile.oauthstorage.AuthenticationSharedPreferencesStorage

class SharedPreferencesManager private constructor(authenticationLocalStorage: AuthenticationLocalStorage) :
    AuthenticationLocalStorage by authenticationLocalStorage {

    constructor(context: Context, encrypted: Boolean = false, compat: Boolean = false) : this(createAuthenticationLocalStorage(context, encrypted, compat))


    companion object {
        private fun createAuthenticationLocalStorage(context: Context, encrypted: Boolean, compat: Boolean): AuthenticationLocalStorage =
            when {
                compat -> AuthenticationSecureSharedPreferencesStorageCompat(context)
                encrypted -> AuthenticationSecureSharedPreferencesStorage.create(context)
                else -> AuthenticationSharedPreferencesStorage(context)
            }
    }
}