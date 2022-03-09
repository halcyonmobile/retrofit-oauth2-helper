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

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Simple interceptor which adds the given [clientId] as query parameter to the request.
 */
internal class ClientIdParameterInterceptor(private val clientId: String) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()

        val url = original.url.newBuilder()
            .addQueryParameter("client_id", clientId)
            .addQueryParameter("clientId", clientId)
            .build()

        return chain.proceed(original.newBuilder().url(url).build())
    }
}