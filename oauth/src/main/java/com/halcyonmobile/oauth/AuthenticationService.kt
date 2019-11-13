/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth

import retrofit2.Call

/**
 * Service which is used by the [com.halcyonmobile.oauth.internal.Authenticator].
 *
 * It is bridge between the [com.halcyonmobile.oauth.internal.Authenticator] and a user defined retrofit service which
 * ultimately does the request.
 *
 * The instance will be created by the [AuthenticationServiceAdapter].
 */
interface AuthenticationService {

    fun refreshToken(refreshToken: String): Call<out SessionDataResponse>
}