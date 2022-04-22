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
package com.halcyonmobile.oauthgson

import com.halcyonmobile.oauth.IsSessionExpiredException as DeprecatedIsSessionExpiredException
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.halcyonmobile.oauth.OauthRetrofitContainer
import com.halcyonmobile.oauth.OauthRetrofitContainerBuilder
import com.halcyonmobile.oauth.SessionDataResponse
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.IsSessionExpiredException
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauthparsing.AuthenticationServiceAdapterImpl
import com.halcyonmobile.oauthparsing.OauthRetrofitWithParserContainerBuilder
import com.halcyonmobile.oauthparsing.RefreshServiceFieldParameterProvider
import com.halcyonmobile.oauthparsing.RefreshTokenService
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Builder class for [OauthRetrofitContainer] which uses moshi as it's [retrofit2.Converter.Factory].
 *
 * It has a default parser, which can be disabled, see [disableDefaultParsing].
 */
class OauthRetrofitWithGsonContainerBuilder(
    clientId: String,
    authenticationLocalStorage: AuthenticationLocalStorage,
    sessionExpiredEventHandler: SessionExpiredEventHandler
) : OauthRetrofitWithParserContainerBuilder<OauthRetrofitWithGsonContainerBuilder, Gson> {

    private var disableDefaultParsing = false
    private val gsonBuilder = GsonBuilder()
    private val authenticationServiceAdapterImpl = AuthenticationServiceAdapterImpl()
    private var oauthRetrofitContainerBuilder = OauthRetrofitContainerBuilder(
        clientId = clientId,
        adapter = authenticationServiceAdapterImpl,
        sessionExpiredEventHandler = sessionExpiredEventHandler,
        authenticationLocalStorage = authenticationLocalStorage,
        refreshServiceClass = RefreshTokenService::class
    )

    override fun setRefreshServicePath(refreshServicePath: String): OauthRetrofitWithGsonContainerBuilder = apply {
        authenticationServiceAdapterImpl.refreshServicePath = refreshServicePath
    }

    override fun setRefreshTokenFieldName(refreshTokenFieldName: String): OauthRetrofitWithGsonContainerBuilder =
        apply {
            authenticationServiceAdapterImpl.refreshTokenFieldName = refreshTokenFieldName
        }

    override fun setGrantType(grantType: String?) = apply {
        authenticationServiceAdapterImpl.grantType = grantType
    }

    override fun setRefreshServiceFieldParameterProvider(refreshServiceFieldParameterProvider: RefreshServiceFieldParameterProvider?): OauthRetrofitWithGsonContainerBuilder =
        apply {
            authenticationServiceAdapterImpl.refreshServiceFieldParameterProvider = refreshServiceFieldParameterProvider
        }

    override fun configureSessionlessOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder): OauthRetrofitWithGsonContainerBuilder =
        apply {
            oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureSessionlessOkHttpClient(configure)
        }

    override fun configureSessionOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder): OauthRetrofitWithGsonContainerBuilder =
        apply {
            oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureSessionOkHttpClient(configure)
        }

    override fun configureBothOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder): OauthRetrofitWithGsonContainerBuilder =
        apply {
            oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureBothOkHttpClient(configure)
        }

    override fun configureRetrofit(configure: Retrofit.Builder.() -> Retrofit.Builder): OauthRetrofitWithGsonContainerBuilder =
        apply {
            oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureRetrofit(configure)
        }

    override fun disableDefaultParsing() = apply {
        disableDefaultParsing = true
    }

    override fun setIsSessionExpiredExceptionDecider(isSessionExpiredException: IsSessionExpiredException) = apply {
        oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.setIsSessionExpiredExceptionDecider(isSessionExpiredException)
    }

    override fun setIsSessionExpiredExceptionDecider(isSessionExpiredException: DeprecatedIsSessionExpiredException) = apply {
        oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.setIsSessionExpiredExceptionDecider(isSessionExpiredException)
    }

    /**
     * Configuration for the [Gson] instance.
     */
    fun configureGson(configure: GsonBuilder.() -> GsonBuilder): OauthRetrofitWithGsonContainerBuilder =
        apply {
            configure(gsonBuilder)
        }

    override fun build(): OauthRetrofitContainerWithGson {
        val gson = gsonBuilder
            .let {
                if (disableDefaultParsing) {
                    it
                } else {
                    it.registerTypeAdapter(SessionDataResponse::class.java, SessionDataResponseDeserializer())
                }
            }
            .create()
        oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureRetrofit {
            addConverterFactory(GsonConverterFactory.create(gson))
        }
        return OauthRetrofitContainerWithGson(oauthRetrofitContainerBuilder.build(), gson)
    }

}