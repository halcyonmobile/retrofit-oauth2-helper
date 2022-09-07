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
import com.halcyonmobile.oauth.dependencies.TokenExpirationStorage
import com.halcyonmobile.oauthsecurestorage.AuthenticationSecureSharedPreferencesStorage
import com.halcyonmobile.oauthsecurestorage.CombinedSecureSharedPreferencesStorage
import com.halcyonmobile.oauthsecurestoragecompat.AuthenticationSecureSharedPreferencesStorageCompat
import com.halcyonmobile.oauthsecurestoragecompat.CombinedSecureSharedPreferencesStorageCompat
import com.halcyonmobile.oauthstorage.AuthenticationSharedPreferencesStorage
import com.halcyonmobile.oauthstorage.CombinedSharedPreferencesStorage

class SharedPreferencesManager private constructor(
    private val combinedSharedPreferencesStorage: CombinedSharedPreferencesStorage
) : AuthenticationLocalStorage by combinedSharedPreferencesStorage, TokenExpirationStorage by combinedSharedPreferencesStorage {

    constructor(context: Context, type: Type = Type.ONLY_AUTH, variant: Variant = Variant.DEFAULT) : this(createAuthenticationLocalStorage(context, type, variant))

    override fun clear() {
        combinedSharedPreferencesStorage.clear()
    }

    enum class Type {
        ONLY_AUTH, WITH_TOKEN_EXPIRATION
    }

    enum class Variant {
        COMPAT, ENCRYPTED, DEFAULT
    }

    companion object {

        private fun createAuthenticationLocalStorage(context: Context, type: Type, variant: Variant): CombinedSharedPreferencesStorage =
            when (type) {
                Type.ONLY_AUTH -> {
                    val authStorage = when (variant) {
                        Variant.COMPAT -> AuthenticationSecureSharedPreferencesStorageCompat(context)
                        Variant.ENCRYPTED -> AuthenticationSecureSharedPreferencesStorage.create(context)
                        Variant.DEFAULT -> AuthenticationSharedPreferencesStorage(context)
                    }

                    CombinedSharedPreferencesStorage(authStorage, NeverExpiredTokenExpirationStorage())
                }
                Type.WITH_TOKEN_EXPIRATION -> {
                    when (variant) {
                        Variant.COMPAT -> CombinedSecureSharedPreferencesStorageCompat.create(context)
                        Variant.ENCRYPTED -> CombinedSecureSharedPreferencesStorage.create(context)
                        Variant.DEFAULT -> CombinedSharedPreferencesStorage.create(context)
                    }
                }
            }

        private class NeverExpiredTokenExpirationStorage : TokenExpirationStorage {

            override var accessTokenExpiresAt: Long
                get() = Long.MAX_VALUE
                set(value) = Unit

            override fun clear() = Unit
        }
    }
}