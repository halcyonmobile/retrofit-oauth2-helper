/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthgson

import com.google.gson.annotations.SerializedName
import com.halcyonmobile.oauth.SessionDataResponse

/**
 * Default model implementing [SessionDataResponse].
 */
data class RefreshTokenResponse(
    @SerializedName("user_id") override val userId: String,
    @SerializedName("access_token") override val token: String,
    @SerializedName("refresh_token") override val refreshToken: String,
    @SerializedName("token_type") override val tokenType: String
) : SessionDataResponse