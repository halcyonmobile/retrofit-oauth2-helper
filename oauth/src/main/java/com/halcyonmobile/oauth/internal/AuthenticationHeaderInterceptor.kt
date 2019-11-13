/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth.internal

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Interceptor which adds the Authorization token to the header of the request.
 */
internal class AuthenticationHeaderInterceptor(private val setAuthorizationHeader: SetAuthorizationHeaderUseCase) : Interceptor {

    @Throws(IOException::class)
    override fun intercept(chain: Interceptor.Chain): Response =
        chain.proceed(setAuthorizationHeader(chain.request()))
}