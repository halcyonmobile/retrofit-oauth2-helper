/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthmoshi

import com.halcyonmobile.oauth.SessionDataResponse
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Default model implementing [SessionDataResponse].
 */
@JsonClass(generateAdapter = true)
data class RefreshTokenResponse(
    @Json(name = "user_id") override val userId: String,
    @Json(name = "access_token") override val token: String,
    @Json(name = "refresh_token") override val refreshToken: String,
    @Json(name = "token_type") override val tokenType: String
) : SessionDataResponse