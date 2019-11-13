/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.core.basicsetup

import com.halcyonmobile.oauth.SessionDataResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    @field:Json(name = "user_id") override val userId: String,
    @field:Json(name = "access_token") override val token: String,
    @field:Json(name = "refresh_token") override val refreshToken: String,
    @field:Json(name = "token_type") override val tokenType: String
) : SessionDataResponse