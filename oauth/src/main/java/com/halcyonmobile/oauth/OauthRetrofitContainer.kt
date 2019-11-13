/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth

import retrofit2.Retrofit

/**
 * The end result of the [OauthRetrofitContainerBuilder].
 *
 * Contains two instance of [Retrofit]:
 *  - [sessionlessRetrofit] contains a [com.halcyonmobile.oauth.internal.ClientIdParameterInterceptor] and should be used
 *  for requests which doesn't require session such as sign-in
 *  - [sessionRetrofit] contains an [com.halcyonmobile.oauth.internal.Authenticator] which refreshes the expired token
 *  when a running request gets 401 - Unauthorized and an [com.halcyonmobile.oauth.internal.AuthenticationHeaderInterceptor]
 *  which adds an Authorization header.
 *
 *  The Session is saved in the [com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage] and SessionExpiration
 *  is notified via [com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler].
 */
class OauthRetrofitContainer(val sessionRetrofit: Retrofit, val sessionlessRetrofit: Retrofit)