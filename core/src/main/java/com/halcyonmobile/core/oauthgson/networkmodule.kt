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
package com.halcyonmobile.core.oauthgson

import com.halcyonmobile.core.ExampleRemoteSource
import com.halcyonmobile.core.SessionExampleService
import com.halcyonmobile.core.SessionlessExampleService
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauthgson.OauthRetrofitContainerWithGson
import com.halcyonmobile.oauthgson.OauthRetrofitWithGsonContainerBuilder
import com.halcyonmobile.oauthmoshikoin.NON_SESSION_RETROFIT
import com.halcyonmobile.oauthmoshikoin.SESSION_RETROFIT
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Example network module which uses the [com.halcyonmobile.oauthgson.OauthRetrofitContainerWithGson] to create the needed two
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
            single {
                OauthRetrofitWithGsonContainerBuilder(
                    clientId = clientId,
                    authenticationLocalStorage = provideAuthenticationLocalStorage(),
                    sessionExpiredEventHandler = provideSessionExpiredEventHandler()
                )
                    .configureRetrofit {
                        baseUrl(baseUrl)
                    }
                    .build()
            }
            single(SESSION_RETROFIT) { get<OauthRetrofitContainerWithGson>().oauthRetrofitContainer.sessionRetrofit }
            single(NON_SESSION_RETROFIT) { get<OauthRetrofitContainerWithGson>().oauthRetrofitContainer.sessionlessRetrofit }
            single { get<OauthRetrofitContainerWithGson>().gson }
        }
    )
}