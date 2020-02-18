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

import com.halcyonmobile.oauth.IsSessionExpiredException
import retrofit2.HttpException
import java.net.HttpURLConnection

/**
 * Default implementation of [IsSessionExpiredException].
 *
 * Checks the [HttpException] against to most common session expiration responses.
 */
class DefaultIsSessionExpiredException: IsSessionExpiredException{
    override fun invoke(httpException: HttpException): Boolean =
        httpException.isInvalidTokenException() || httpException.isExpiredTokenException()

    companion object {
        fun HttpException.isInvalidTokenException() =
            code() == HttpURLConnection.HTTP_BAD_REQUEST &&
                    errorBodyAsString().contains("\"Invalid refresh token:")

        fun HttpException.isExpiredTokenException() =
            code() == HttpURLConnection.HTTP_UNAUTHORIZED &&
                    errorBodyAsString().contains("\"Invalid refresh token (expired):")

        fun HttpException.errorBodyAsString() = response()?.errorBody()?.string().orEmpty()
    }
}