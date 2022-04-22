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
package com.halcyonmobile.oauthparsing

import com.halcyonmobile.oauth.OauthRetrofitContainer
import com.halcyonmobile.oauth.SessionDataResponse
import com.halcyonmobile.oauth.dependencies.IsSessionExpiredException
import com.halcyonmobile.oauth.internal.DefaultIsSessionExpiredException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import com.halcyonmobile.oauth.IsSessionExpiredException as DeprecatedIsSessionExpiredException

/**
 * Builder interface for [OauthRetrofitContainer] which uses a parser as it's [retrofit2.Converter.Factory].
 */
interface OauthRetrofitWithParserContainerBuilder<T, Parser> {

    /**
     * Sets the path of the refresh service, the default is "oauth/token", see [AuthenticationServiceAdapterImpl]
     */
    fun setRefreshServicePath(refreshServicePath: String): T

    /**
     * Sets the field name of the refresh token, the default is "refresh_token", see [AuthenticationServiceAdapterImpl]
     */
    fun setRefreshTokenFieldName(refreshTokenFieldName: String): T

    /**
     * Sets the grant type of the service, the default is "refresh_token", see [AuthenticationServiceAdapterImpl].
     * Setting it null will disable adding the grant_type field, so it won't be added similar as grant_type=refresh_token
     */
    fun setGrantType(grantType: String?): T

    /**
     * Sets a [RefreshServiceFieldParameterProvider] which can provide additional field params to the [RefreshTokenService].
     */
    fun setRefreshServiceFieldParameterProvider(refreshServiceFieldParameterProvider: RefreshServiceFieldParameterProvider?): T

    /**
     * Additional configuration for the [OkHttpClient] used by the sessionless-retrofit instance
     */
    fun configureSessionlessOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder): T

    /**
     * Additionally configuration for the [OkHttpClient] used by the session-retrofit instance
     */
    fun configureSessionOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder): T

    /**
     * Additional configuration for the [OkHttpClient] used by all retrofit instances provided.
     * Example usage: Logging.
     */
    fun configureBothOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder): T

    /**
     * Configuration for the [Retrofit] instance, here you should define your baseUrl, parsing etc.
     */
    fun configureRetrofit(configure: Retrofit.Builder.() -> Retrofit.Builder): T

    /**
     * Sets a class which decided what should be considered sessionExpiration.
     * By default a response containing "Invalid refresh token" or "Invalid refresh token (expired):" is considered, see [DefaultIsSessionExpiredException]
     */
    fun setIsSessionExpiredExceptionDecider(isSessionExpiredException: IsSessionExpiredException) : T

    /**
     * Sets a class which decided what should be considered sessionExpiration.
     * By default a response containing "Invalid refresh token" or "Invalid refresh token (expired):" is considered, see [DefaultIsSessionExpiredException]
     */
    fun setIsSessionExpiredExceptionDecider(isSessionExpiredException: DeprecatedIsSessionExpiredException) : T

    /**
     * Disables the default parsing.
     * In this case the user is responsible to setup a JsonAdapter which parses [SessionDataResponse] and add it to the parser
     */
    fun disableDefaultParsing(): T

    /**
     * Builds the container which holds the [Retrofit] instances and parser.
     */
    fun build(): OauthRetrofitContainerWithParser<Parser>

}