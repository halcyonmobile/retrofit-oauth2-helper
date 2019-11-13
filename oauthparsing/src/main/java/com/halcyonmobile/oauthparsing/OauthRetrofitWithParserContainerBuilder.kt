/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthparsing

import com.halcyonmobile.oauth.IsSessionExpiredException
import com.halcyonmobile.oauth.OauthRetrofitContainer
import com.halcyonmobile.oauth.SessionDataResponse
import com.halcyonmobile.oauth.internal.DefaultIsSessionExpiredException
import okhttp3.OkHttpClient
import retrofit2.Retrofit

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
     * Disables the default parsing.
     * In this case the user is responsible to setup a JsonAdapter which parses [SessionDataResponse] and add it to the parser
     */
    fun disableDefaultParsing(): T

    /**
     * Builds the container which holds the [Retrofit] instances and parser.
     */
    fun build(): OauthRetrofitContainerWithParser<Parser>

}