/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
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