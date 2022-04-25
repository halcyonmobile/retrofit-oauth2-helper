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

import com.halcyonmobile.oauth.AuthenticationService
import com.halcyonmobile.oauth.INVALIDATION_AFTER_REFRESH_HEADER_NAME
import com.halcyonmobile.oauth.INVALIDATION_AFTER_REFRESH_HEADER_VALUE
import com.halcyonmobile.oauth.SessionDataResponse
import com.halcyonmobile.oauth.authFinishedInvalidationException
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.IsSessionExpiredException
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauth.save
import java.io.IOException
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import retrofit2.HttpException

/**
 * Synchronized [okhttp3.Authenticator] which refreshes the token when
 * the request response is 401 - Unauthorized.
 *
 * @param refreshTokenService is used to run the token-refreshing
 *     request returning a [SessionDataResponse]
 * @param authenticationLocalStorage the persistent storage for the
 *     session [SessionDataResponse]
 * @param setAuthorizationHeader an internal use-case which adds the
 *     authorization header to the request based on the stored session.
 * @param isSessionExpiredException the component defining if an
 *     exception can be considered SessionExpired exception.
 * @param sessionExpiredEventHandler a listener for session expiration.
 */
internal class Authenticator(
    private val refreshTokenService: AuthenticationService,
    private val authenticationLocalStorage: AuthenticationLocalStorage,
    private val setAuthorizationHeader: SetAuthorizationHeaderUseCase,
    private val isSessionExpiredException: IsSessionExpiredException,
    private val sessionExpiredEventHandler: SessionExpiredEventHandler
) : Authenticator {

    @Throws(IOException::class)
    override fun authenticate(route: Route?, response: Response): Request? {
        synchronized(this) {
            if (!setAuthorizationHeader.isSame(response.request)) {
                return setAuthorizationHeader(response.request)
            } else if (authenticationLocalStorage.refreshToken.isEmpty()) {
                return null
            }

            repeat(REFRESH_TOKEN_RETRY_COUNT) {
                try {
                    val refreshTokenResponse = refreshTokenService.refreshToken(authenticationLocalStorage.refreshToken).execute()
                    val sessionDataResponse: SessionDataResponse? = refreshTokenResponse.body()
                    if (refreshTokenResponse.isSuccessful && sessionDataResponse != null) {
                        authenticationLocalStorage.save(sessionDataResponse)

                        // throw exception since the header is present
                        if (response.request.header(INVALIDATION_AFTER_REFRESH_HEADER_NAME) == INVALIDATION_AFTER_REFRESH_HEADER_VALUE) {
                            throw authFinishedInvalidationException
                        }

                        // retry request with the new tokens
                        return setAuthorizationHeader(response.request)
                    } else {
                        throw HttpException(refreshTokenResponse)
                    }
                } catch (throwable: Throwable) {
                    when (throwable) {
                        authFinishedInvalidationException -> throw throwable
                        else -> {
                            if (isSessionExpiredException(throwable)) {
                                onSessionExpiration()
                                return null
                            }
                        }
                    }
                    throwable.printStackTrace()
                }
            }

            // return the request with 401 error since the refresh token failed 3 times.
            return null
        }
    }

    /** On SessionExpiration we clear the data and report the event. */
    private fun onSessionExpiration() {
        authenticationLocalStorage.clear()
        sessionExpiredEventHandler.onSessionExpired()
    }

    companion object {
        private const val REFRESH_TOKEN_RETRY_COUNT = 3
    }
}