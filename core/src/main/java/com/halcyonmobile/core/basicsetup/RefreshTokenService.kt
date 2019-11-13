/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.core.basicsetup

import com.halcyonmobile.oauth.dependencies.RefreshService
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * You have to define a service which will do the token refreshing.
 *
 * In order to use [RefreshService] annotation to generate the [com.halcyonmobile.oauth.RefreshTokenServiceAuthenticationServiceAdapter]
 * you must have only one function, which returns a [Call] with a typeParameter subtype of [com.halcyonmobile.oauth.SessionDataResponse]
 * Your first parameter must be the refresh token, every other parameter has to have default value.
 * If that's not possible consider creating your own specific implementation for the [com.halcyonmobile.oauth.AuthenticationServiceAdapter].
 */
@RefreshService
interface RefreshTokenService {

    @POST("oauth/token")
    @FormUrlEncoded
    fun refresh(@Field("refresh_token") refreshToken: String, @Field("grant_type") grantType: String = "refresh_token"): Call<RefreshTokenResponse>
}