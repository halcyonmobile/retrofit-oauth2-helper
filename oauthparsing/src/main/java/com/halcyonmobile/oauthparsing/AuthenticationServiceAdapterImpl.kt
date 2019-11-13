/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
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