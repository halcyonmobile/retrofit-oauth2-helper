/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.core.basicsetup

import com.halcyonmobile.core.ExampleRemoteSource
import com.halcyonmobile.core.SessionExampleService
import com.halcyonmobile.core.SessionlessExampleService
import com.halcyonmobile.oauth.OauthRetrofitContainer
import com.halcyonmobile.oauth.OauthRetrofitContainerBuilder
import com.halcyonmobile.oauth.RefreshTokenServiceAuthenticationServiceAdapter
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauthmoshikoin.NON_SESSION_RETROFIT
import com.halcyonmobile.oauthmoshikoin.SESSION_RETROFIT
import com.squareup.moshi.Moshi
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Example network module which uses the [com.halcyonmobile.oauth.OauthRetrofitContainerBuilder] to create the needed two
 * retrofit instance, then provides two example service [SessionExampleService] [SessionlessExampleService].
 */
fun createNetworkModules(
    clientId: String,
    baseUrl: String,
    provideAuthenticationLocalStorage: Scope.() -> AuthenticationLocalStorage,
    provideSessionExpiredEventHandler: Scope.() -> SessionExpiredEventHandler
): List<Module> {
    return listOf(
        module {
            factory { get<Retrofit>(SESSION_RETROFIT).create(SessionExampleService::class.java) }
            factory { get<Retrofit>(NON_SESSION_RETROFIT).create(SessionlessExampleService::class.java) }
            factory { ExampleRemoteSource(get(), get()) }
        },
        module {
            single { provideAuthenticationLocalStorage() }
            single { provideSessionExpiredEventHandler() }
            single { Moshi.Builder().build() }
            single {
                OauthRetrofitContainerBuilder(
                    clientId = clientId,
                    refreshServiceClass = RefreshTokenService::class,
                    authenticationLocalStorage = provideAuthenticationLocalStorage(),
                    sessionExpiredEventHandler = provideSessionExpiredEventHandler(),
                    adapter = RefreshTokenServiceAuthenticationServiceAdapter()
                )
                    .configureRetrofit {
                        baseUrl(baseUrl).addConverterFactory(MoshiConverterFactory.create(get()))
                    }
                    .build()
            }
            single(SESSION_RETROFIT) { get<OauthRetrofitContainer>().sessionRetrofit }
            single(NON_SESSION_RETROFIT) { get<OauthRetrofitContainer>().sessionlessRetrofit }
        }
    )
}