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
package com.halcyonmobile.core.oauthmoshikoin

import com.halcyonmobile.core.ExampleRemoteSource
import com.halcyonmobile.core.SessionExampleService
import com.halcyonmobile.core.SessionlessExampleService
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauthmoshikoin.NON_SESSION_RETROFIT
import com.halcyonmobile.oauthmoshikoin.SESSION_RETROFIT
import com.halcyonmobile.oauthmoshikoin.createOauthModule
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * Example network module which uses the [com.halcyonmobile.oauthmoshikoin.createOauthModule] to create and provide the
 * needed two retrofit instance and moshi.
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
        createOauthModule(
            clientId = clientId,
            provideSessionExpiredEventHandler = provideSessionExpiredEventHandler,
            provideAuthenticationLocalStorage = provideAuthenticationLocalStorage,
            configureRetrofit = {
                it.baseUrl(baseUrl)
            }
        )
    )
}