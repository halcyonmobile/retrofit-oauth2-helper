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