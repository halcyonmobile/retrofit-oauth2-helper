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