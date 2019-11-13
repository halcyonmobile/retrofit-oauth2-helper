/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
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