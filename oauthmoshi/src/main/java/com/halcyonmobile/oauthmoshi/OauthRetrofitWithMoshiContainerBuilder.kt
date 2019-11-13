/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauthmoshi

import com.halcyonmobile.oauth.IsSessionExpiredException
import com.halcyonmobile.oauth.OauthRetrofitContainer
import com.halcyonmobile.oauth.OauthRetrofitContainerBuilder
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauthparsing.AuthenticationServiceAdapterImpl
import com.halcyonmobile.oauthparsing.OauthRetrofitWithParserContainerBuilder
import com.halcyonmobile.oauthparsing.RefreshServiceFieldParameterProvider
import com.halcyonmobile.oauthparsing.RefreshTokenService
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Builder class for [OauthRetrofitContainer] which uses moshi as it's [retrofit2.Converter.Factory].
 *
 * It has a default parser, which can be disabled, see [disableDefaultParsing].
 */
class OauthRetrofitWithMoshiContainerBuilder(
    clientId: String,
    authenticationLocalStorage: AuthenticationLocalStorage,
    sessionExpiredEventHandler: SessionExpiredEventHandler
) : OauthRetrofitWithParserContainerBuilder<OauthRetrofitWithMoshiContainerBuilder, Moshi> {

    private var disableDefaultParsing = false
    private val moshiBuilder = Moshi.Builder()
    private val authenticationServiceAdapterImpl = AuthenticationServiceAdapterImpl()
    private var oauthRetrofitContainerBuilder = OauthRetrofitContainerBuilder(
        clientId = clientId,
        adapter = authenticationServiceAdapterImpl,
        sessionExpiredEventHandler = sessionExpiredEventHandler,
        authenticationLocalStorage = authenticationLocalStorage,
        refreshServiceClass = RefreshTokenService::class
    )

    override fun setRefreshServicePath(refreshServicePath: String): OauthRetrofitWithMoshiContainerBuilder = apply {
        authenticationServiceAdapterImpl.refreshServicePath = refreshServicePath
    }

    override fun setRefreshTokenFieldName(refreshTokenFieldName: String): OauthRetrofitWithMoshiContainerBuilder =
        apply {
            authenticationServiceAdapterImpl.refreshTokenFieldName = refreshTokenFieldName
        }

    override fun setGrantType(grantType: String?): OauthRetrofitWithMoshiContainerBuilder = apply {
        authenticationServiceAdapterImpl.grantType = grantType
    }


    override fun setRefreshServiceFieldParameterProvider(refreshServiceFieldParameterProvider: RefreshServiceFieldParameterProvider?): OauthRetrofitWithMoshiContainerBuilder =
        apply {
            authenticationServiceAdapterImpl.refreshServiceFieldParameterProvider = refreshServiceFieldParameterProvider
        }

    override fun configureSessionlessOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder): OauthRetrofitWithMoshiContainerBuilder =
        apply {
            oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureSessionlessOkHttpClient(configure)
        }

    override fun configureSessionOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder): OauthRetrofitWithMoshiContainerBuilder =
        apply {
            oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureSessionOkHttpClient(configure)
        }

    override fun configureBothOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder): OauthRetrofitWithMoshiContainerBuilder =
        apply {
            oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureBothOkHttpClient(configure)
        }

    override fun configureRetrofit(configure: Retrofit.Builder.() -> Retrofit.Builder): OauthRetrofitWithMoshiContainerBuilder =
        apply {
            oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureRetrofit(configure)
        }

    override fun disableDefaultParsing() = apply {
        disableDefaultParsing = true
    }

    override fun setIsSessionExpiredExceptionDecider(isSessionExpiredException: IsSessionExpiredException) = apply{
        oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.setIsSessionExpiredExceptionDecider(isSessionExpiredException)
    }

    /**
     * Configuration for the [Moshi] instance.
     */
    fun configureMoshi(configure: Moshi.Builder.() -> Moshi.Builder): OauthRetrofitWithMoshiContainerBuilder =
        apply {
            configure(moshiBuilder)
        }

    override fun build(): OauthRetrofitContainerWithMoshi {
        val moshi = moshiBuilder
            .let {
                if (disableDefaultParsing) {
                    it
                } else {
                    it.add(RefreshTokenResponseWrapper())
                }
            }
            .build()
        oauthRetrofitContainerBuilder = oauthRetrofitContainerBuilder.configureRetrofit {
            addConverterFactory(MoshiConverterFactory.create(moshi))
        }
        return OauthRetrofitContainerWithMoshi(oauthRetrofitContainerBuilder.build(), moshi)
    }

}