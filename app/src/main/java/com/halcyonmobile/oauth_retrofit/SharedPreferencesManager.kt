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