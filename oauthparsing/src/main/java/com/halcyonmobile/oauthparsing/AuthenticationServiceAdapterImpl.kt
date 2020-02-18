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
package com.halcyonmobile.oauthparsing

import com.halcyonmobile.oauth.AuthenticationService
import com.halcyonmobile.oauth.AuthenticationServiceAdapter
import com.halcyonmobile.oauth.SessionDataResponse
import retrofit2.Call

/**
 * [AuthenticationServiceAdapter] implementation which uses [RefreshTokenService]
 */
class AuthenticationServiceAdapterImpl : AuthenticationServiceAdapter<RefreshTokenService> {

    var refreshServiceFieldParameterProvider: RefreshServiceFieldParameterProvider? = null
    var refreshServicePath: String = DEFAULT_REFRESH_SERVICE_PATH
    var refreshTokenFieldName: String = REFRESH_TOKEN_DEFAULT_FIELD_NAME
    var grantType: String? = DEFAULT_GRANT_TYPE

    override fun adapt(service: RefreshTokenService): AuthenticationService =
        AuthenticationServiceImpl(
            refreshServiceFieldParameterProvider = refreshServiceFieldParameterProvider,
            refreshTokenQueryName = refreshTokenFieldName,
            refreshServicePath = refreshServicePath,
            grantType = grantType,
            service = service
        )

    class AuthenticationServiceImpl(
        private val refreshServiceFieldParameterProvider: RefreshServiceFieldParameterProvider?,
        private val refreshTokenQueryName: String,
        private val refreshServicePath: String,
        private val grantType: String?,
        private val service: RefreshTokenService
    ) : AuthenticationService {

        override fun refreshToken(refreshToken: String): Call<out SessionDataResponse> {
            val fieldMap = refreshServiceFieldParameterProvider?.get(refreshToken)?.toMutableMap() ?: mutableMapOf()
            fieldMap[refreshTokenQueryName] = refreshToken
            grantType?.let { fieldMap[GRANT_TYPE] = it }
            return service.refresh(refreshServicePath, fieldMap)
        }
    }

    companion object {
        private const val GRANT_TYPE = "grant_type"
        private const val REFRESH_TOKEN_DEFAULT_FIELD_NAME = "refresh_token"
        private const val DEFAULT_GRANT_TYPE = "refresh_token"
        private const val DEFAULT_REFRESH_SERVICE_PATH = "oauth/token"
    }
}