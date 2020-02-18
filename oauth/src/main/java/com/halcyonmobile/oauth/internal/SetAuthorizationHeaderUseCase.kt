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
package com.halcyonmobile.oauth.internal

import androidx.annotation.CheckResult
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import okhttp3.Request

/**
 * Sets the authorization header to the given request with the tokens from [authenticationLocalStorage] and returns a new request instance.
 */
internal class SetAuthorizationHeaderUseCase(private val authenticationLocalStorage: AuthenticationLocalStorage) {

    /**
     * Adds the authentication header to the given [request].
     */
    @CheckResult
    operator fun invoke(request: Request): Request =
        request.newBuilder()
            .header(AUTHORIZATION_KEY, "${authenticationLocalStorage.tokenType} ${authenticationLocalStorage.accessToken}")
            .build()

    /**
     * Checks if the given request already contains the same authentication header.
     */
    fun isSame(request: Request): Boolean = invoke(request).header(AUTHORIZATION_KEY) == request.header(AUTHORIZATION_KEY)

    companion object {
        private const val AUTHORIZATION_KEY = "Authorization"
    }
}