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
package com.halcyonmobile.oauthmoshikoin

import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.IsSessionExpiredException
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauthmoshi.OauthRetrofitContainerWithMoshi
import com.halcyonmobile.oauthmoshi.OauthRetrofitWithMoshiContainerBuilder
import com.halcyonmobile.oauthparsing.RefreshServiceFieldParameterProvider
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.core.qualifier.StringQualifier
import org.koin.core.scope.Scope
import org.koin.dsl.module
import retrofit2.Retrofit

/**
 * All in one method to create a module for the retrofit and it's necessities created with oauth.
 *
 * @param clientId The clientId used by non-session requests, see [OauthRetrofitWithMoshiContainerBuilder]
 * @param provideAuthenticationLocalStorage lambda which is able to create an [AuthenticationLocalStorage] from a koin scope. Note: The created instance will be provided from the returned module. see [OauthRetrofitWithMoshiContainerBuilder]
 * @param provideSessionExpiredEventHandler lambda which is able to create a [SessionExpiredEventHandler] from a koin scope. Note: The created instance will be provided from the returned module. see [OauthRetrofitWithMoshiContainerBuilder]
 * @param configureRetrofit a lambda to configure both retrofit instances. here you minimum should set your base url.  see [OauthRetrofitWithMoshiContainerBuilder.configureRetrofit]
 * @param disableDefaultParsing disables the default parsing, so the user will be responsible for it, see [OauthRetrofitWithMoshiContainerBuilder.disableDefaultParsing]
 * @param refreshServicePath sets the path for the token-refresh request, see [OauthRetrofitWithMoshiContainerBuilder.setRefreshServicePath]
 * @param refreshTokenFieldName sets the name of the refresh token parameter, see [OauthRetrofitWithMoshiContainerBuilder.setRefreshTokenFieldName]
 * @param grantType sets the grantType, empty string means the default is used, see [OauthRetrofitWithMoshiContainerBuilder.setGrantType]
 * @param refreshServiceFieldParameterProvider lambda which is able to create a [RefreshServiceFieldParameterProvider] from koin scope. Note: The created instance will NOT be provided from the returned module. see [OauthRetrofitWithMoshiContainerBuilder.setRefreshServiceFieldParameterProvider]
 * @param configureBothOkHttpClient lambda to configure both [OkHttpClient], see [OauthRetrofitWithMoshiContainerBuilder.configureBothOkHttpClient]
 * @param configureSessionOkHttpClient lambda to configure [OkHttpClient] with session, see [OauthRetrofitWithMoshiContainerBuilder.configureSessionOkHttpClient]
 * @param configureSessionlessOkHttpClient lambda to configure [OkHttpClient] without session, see [OauthRetrofitWithMoshiContainerBuilder.configureSessionlessOkHttpClient]
 * @param configureMoshi lambda to configure [Moshi]. see [OauthRetrofitWithMoshiContainerBuilder.configureMoshi]
 * @param provideIsSessionExpiredException lambda which is able to create a [IsSessionExpiredException] from koin scope. Note: The created instance will NOT be provided from the returned module. see [OauthRetrofitWithMoshiContainerBuilder.setIsSessionExpiredExceptionDecider]
 *
 * @return a koin module which provides a [AuthenticationLocalStorage], [SessionExpiredEventHandler], [Moshi], and two [Retrofit] instances as singletons.
 */
inline fun createOauthModule(
    clientId: String,
    crossinline provideAuthenticationLocalStorage: Scope.() -> AuthenticationLocalStorage,
    crossinline provideSessionExpiredEventHandler: Scope.() -> SessionExpiredEventHandler,
    crossinline configureRetrofit: Scope.(Retrofit.Builder) -> Retrofit.Builder,
    disableDefaultParsing: Boolean = false,
    refreshServicePath: String? = null,
    refreshTokenFieldName: String? = null,
    grantType: String? = "",
    noinline refreshServiceFieldParameterProvider: (Scope.() -> RefreshServiceFieldParameterProvider)? = null,
    crossinline configureBothOkHttpClient: Scope.(OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
    crossinline configureSessionOkHttpClient: Scope.(OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
    crossinline configureSessionlessOkHttpClient: Scope.(OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
    crossinline configureMoshi: Scope.(Moshi.Builder) -> Moshi.Builder = { it },
    noinline provideIsSessionExpiredException: (Scope.() -> IsSessionExpiredException)? = null
): Module =
    createOauthModule(
        provideClientId = { clientId},
        provideAuthenticationLocalStorage = provideAuthenticationLocalStorage,
        provideIsSessionExpiredException = provideIsSessionExpiredException,
        configureRetrofit = configureRetrofit,
        disableDefaultParsing = disableDefaultParsing,
        refreshServicePath = refreshServicePath,
        refreshTokenFieldName = refreshTokenFieldName,
        grantType = grantType,
        refreshServiceFieldParameterProvider = refreshServiceFieldParameterProvider,
        configureBothOkHttpClient = configureBothOkHttpClient,
        configureSessionOkHttpClient = configureSessionOkHttpClient,
        configureSessionlessOkHttpClient = configureSessionlessOkHttpClient,
        configureMoshi = configureMoshi,
        provideSessionExpiredEventHandler = provideSessionExpiredEventHandler
    )

/**
 * All in one method to create a module for the retrofit and it's necessities created with oauth.
 *
 * @param provideClientId Providing the clientId used by non-session requests, see [OauthRetrofitWithMoshiContainerBuilder]. Note: the clientId will be requested only once.
 * @param provideAuthenticationLocalStorage lambda which is able to create an [AuthenticationLocalStorage] from a koin scope. Note: The created instance will be provided from the returned module. see [OauthRetrofitWithMoshiContainerBuilder]
 * @param provideSessionExpiredEventHandler lambda which is able to create a [SessionExpiredEventHandler] from a koin scope. Note: The created instance will be provided from the returned module. see [OauthRetrofitWithMoshiContainerBuilder]
 * @param configureRetrofit a lambda to configure both retrofit instances. here you minimum should set your base url.  see [OauthRetrofitWithMoshiContainerBuilder.configureRetrofit]
 * @param disableDefaultParsing disables the default parsing, so the user will be responsible for it, see [OauthRetrofitWithMoshiContainerBuilder.disableDefaultParsing]
 * @param refreshServicePath sets the path for the token-refresh request, see [OauthRetrofitWithMoshiContainerBuilder.setRefreshServicePath]
 * @param refreshTokenFieldName sets the name of the refresh token parameter, see [OauthRetrofitWithMoshiContainerBuilder.setRefreshTokenFieldName]
 * @param grantType sets the grantType, empty string means the default is used, see [OauthRetrofitWithMoshiContainerBuilder.setGrantType]
 * @param refreshServiceFieldParameterProvider lambda which is able to create a [RefreshServiceFieldParameterProvider] from koin scope. Note: The created instance will NOT be provided from the returned module. see [OauthRetrofitWithMoshiContainerBuilder.setRefreshServiceFieldParameterProvider]
 * @param configureBothOkHttpClient lambda to configure both [OkHttpClient], see [OauthRetrofitWithMoshiContainerBuilder.configureBothOkHttpClient]
 * @param configureSessionOkHttpClient lambda to configure [OkHttpClient] with session, see [OauthRetrofitWithMoshiContainerBuilder.configureSessionOkHttpClient]
 * @param configureSessionlessOkHttpClient lambda to configure [OkHttpClient] without session, see [OauthRetrofitWithMoshiContainerBuilder.configureSessionlessOkHttpClient]
 * @param configureMoshi lambda to configure [Moshi]. see [OauthRetrofitWithMoshiContainerBuilder.configureMoshi]
 * @param provideIsSessionExpiredException lambda which is able to create a [IsSessionExpiredException] from koin scope. Note: The created instance will NOT be provided from the returned module. see [OauthRetrofitWithMoshiContainerBuilder.setIsSessionExpiredExceptionDecider]
 *
 * @return a koin module which provides a [AuthenticationLocalStorage], [SessionExpiredEventHandler], [Moshi], and two [Retrofit] instances as singletons.
 */
inline fun createOauthModule(
    crossinline provideClientId: Scope.() -> String,
    crossinline provideAuthenticationLocalStorage: Scope.() -> AuthenticationLocalStorage,
    crossinline provideSessionExpiredEventHandler: Scope.() -> SessionExpiredEventHandler,
    crossinline configureRetrofit: Scope.(Retrofit.Builder) -> Retrofit.Builder,
    disableDefaultParsing: Boolean = false,
    refreshServicePath: String? = null,
    refreshTokenFieldName: String? = null,
    grantType: String? = "",
    noinline refreshServiceFieldParameterProvider: (Scope.() -> RefreshServiceFieldParameterProvider)? = null,
    crossinline configureBothOkHttpClient: Scope.(OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
    crossinline configureSessionOkHttpClient: Scope.(OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
    crossinline configureSessionlessOkHttpClient: Scope.(OkHttpClient.Builder) -> OkHttpClient.Builder = { it },
    crossinline configureMoshi: Scope.(Moshi.Builder) -> Moshi.Builder = { it },
    noinline provideIsSessionExpiredException: (Scope.() -> IsSessionExpiredException)? = null
): Module = module {
    single { provideAuthenticationLocalStorage() }
    single { provideSessionExpiredEventHandler() }

    single {
        OauthRetrofitWithMoshiContainerBuilder(
            clientId = provideClientId(),
            authenticationLocalStorage = get(),
            sessionExpiredEventHandler = get()
        )
            .let { if (refreshServicePath == null) it else it.setRefreshServicePath(refreshServicePath) }
            .let { if (refreshTokenFieldName == null) it else it.setRefreshTokenFieldName(refreshTokenFieldName) }
            .let { if (disableDefaultParsing) it.disableDefaultParsing() else it }
            .let { if (grantType != "") it.setGrantType(grantType) else it }
            .let {
                provideIsSessionExpiredException?.invoke(this)?.let { isSessionExpiredException ->
                    it.setIsSessionExpiredExceptionDecider(isSessionExpiredException)
                } ?: it
            }
            .setRefreshServiceFieldParameterProvider(refreshServiceFieldParameterProvider?.invoke(this))
            .configureRetrofit {
                configureRetrofit(this)
            }
            .configureBothOkHttpClient {
                configureBothOkHttpClient(this)
            }
            .configureSessionOkHttpClient {
                configureSessionOkHttpClient(this)
            }
            .configureSessionlessOkHttpClient {
                configureSessionlessOkHttpClient(this)
            }
            .configureMoshi {
                configureMoshi(this)
            }
            .build()
    }

    single { get<OauthRetrofitContainerWithMoshi>().moshi }
    single(SESSION_RETROFIT) { get<OauthRetrofitContainerWithMoshi>().oauthRetrofitContainer.sessionRetrofit }
    single(NON_SESSION_RETROFIT) { get<OauthRetrofitContainerWithMoshi>().oauthRetrofitContainer.sessionlessRetrofit }
}

val NON_SESSION_RETROFIT: Qualifier = StringQualifier("retrofit_for_request_without_session")
val SESSION_RETROFIT: Qualifier = StringQualifier("retrofit_for_request_with_session")