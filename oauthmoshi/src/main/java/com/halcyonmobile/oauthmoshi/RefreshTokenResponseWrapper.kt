/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthmoshi

import com.halcyonmobile.oauth.SessionDataResponse
import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * JsonAdapter to enable moshi to parse [SessionDataResponse] via [RefreshTokenResponse] JsonAdapter.
 */
class RefreshTokenResponseWrapper {

    @FromJson
    fun fromJson(refreshTokenResponse: RefreshTokenResponse?): SessionDataResponse? = refreshTokenResponse

    @ToJson
    fun toJson(
        jsonWriter: JsonWriter,
        sessionDataResponse: SessionDataResponse,
        delegate: JsonAdapter<RefreshTokenResponse>
    ) {
        val refreshTokenResponse = if (sessionDataResponse is RefreshTokenResponse) {
            sessionDataResponse
        } else {
            RefreshTokenResponse(
                userId = sessionDataResponse.userId,
                refreshToken = sessionDataResponse.refreshToken,
                token = sessionDataResponse.token,
                tokenType = sessionDataResponse.tokenType
            )
        }
        delegate.toJson(jsonWriter, refreshTokenResponse)
    }
}