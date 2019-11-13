/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth.internal

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Simple interceptor which adds the given [clientId] as query parameter to the request.
 */
internal class ClientIdParameterInterceptor(private val clientId: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val url = original.url().newBuilder()
            .addQueryParameter("client_id", clientId)
            .addQueryParameter("clientId", clientId)
            .build()

        return chain.proceed(original.newBuilder().url(url).build())
    }
}