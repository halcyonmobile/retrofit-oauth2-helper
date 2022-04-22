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

import com.halcyonmobile.oauth.IsSessionExpiredException as DeprecatedIsSessionExpiredException
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage
import com.halcyonmobile.oauth.dependencies.IsSessionExpiredException
import com.halcyonmobile.oauth.dependencies.SessionExpiredEventHandler
import com.halcyonmobile.oauth.internal.AuthenticationHeaderInterceptor
import com.halcyonmobile.oauth.internal.Authenticator
import com.halcyonmobile.oauth.internal.ClientIdParameterInterceptor
import com.halcyonmobile.oauth.internal.DefaultIsSessionExpiredException
import com.halcyonmobile.oauth.internal.SetAuthorizationHeaderUseCase
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import kotlin.reflect.KClass

/**
 * Builder for the [OauthRetrofitContainer].
 *
 * @param clientId refers to the clientId header to attach for request which do not require session.
 * @param authenticationLocalStorage A persistent storage which stores the session related data.
 * @param refreshServiceClass This will be used to create a retrofit service which will be called for refreshing the tokens.
 * @param adapter An adapter which creates an [AuthenticationService] from the given [refreshServiceClass] retrofit-service instance.
 * @param sessionExpiredEventHandler A callback for session expiration which means the session is no longer valid and can't be refreshed via tokens.
 */
class OauthRetrofitContainerBuilder<T : Any>(
    private val clientId: String,
    private val authenticationLocalStorage: AuthenticationLocalStorage,
    private val refreshServiceClass: KClass<T>,
    private val adapter: AuthenticationServiceAdapter<T>,
    private val sessionExpiredEventHandler: SessionExpiredEventHandler
) {

    private val retrofitBuilder = Retrofit.Builder()
    private val okHttpClient = OkHttpClient.Builder()
    private var isSessionExpiredException: IsSessionExpiredException = DefaultIsSessionExpiredException()
    private val sessionlessOkHttpClientConfigurations = mutableListOf<OkHttpClient.Builder.() -> OkHttpClient.Builder>()
    private val sessionOkHttpClientConfigurations = mutableListOf<OkHttpClient.Builder.() -> OkHttpClient.Builder>()

    /**
     * Additional configuration for the [okHttpClient] used by the sessionless-retrofit instance
     */
    fun configureSessionlessOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder) = apply {
        sessionlessOkHttpClientConfigurations.add(configure)
    }

    /**
     * Additionally configuration for the [okHttpClient] used by the session-retrofit instance
     */
    fun configureSessionOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder) = apply {
        sessionOkHttpClientConfigurations.add(configure)
    }

    /**
     * Additional configuration for the [okHttpClient] used by all retrofit instances provided.
     * Example usage: Logging.
     */
    fun configureBothOkHttpClient(configure: OkHttpClient.Builder.() -> OkHttpClient.Builder) = apply {
        configureSessionlessOkHttpClient(configure)
        configureSessionOkHttpClient(configure)
    }

    /**
     * Sets a class which decided what should be considered sessionExpiration.
     * By default a response containing "Invalid refresh token" or "Invalid refresh token (expired):" is considered, see [DefaultIsSessionExpiredException]
     */
    fun setIsSessionExpiredExceptionDecider(isSessionExpiredException: IsSessionExpiredException) = apply {
        this.isSessionExpiredException = isSessionExpiredException
    }

    /**
     * Sets a class which decided what should be considered sessionExpiration.
     * By default a response containing "Invalid refresh token" or "Invalid refresh token (expired):" is considered, see [DefaultIsSessionExpiredException]
     */
    fun setIsSessionExpiredExceptionDecider(isSessionExpiredException: DeprecatedIsSessionExpiredException) = apply {
        this.isSessionExpiredException = DeprecatedIsSessionExpiredExceptionAdapter(isSessionExpiredException)
    }

    /**
     * Configuration for the retrofit instance, here you should define your baseUrl, parsing etc.
     */
    fun configureRetrofit(configure: Retrofit.Builder.() -> Retrofit.Builder): OauthRetrofitContainerBuilder<T> = apply {
        configure(retrofitBuilder)
    }

    /**
     * Creates the [OauthRetrofitContainer]
     */
    fun build(): OauthRetrofitContainer {
        val authorizationHeaderUseCase = SetAuthorizationHeaderUseCase(authenticationLocalStorage)
        val okHttpClient = okHttpClient.build()
        val retrofit = retrofitBuilder.build()
        val sessionlessOkHttpClient =
            sessionlessOkHttpClientConfigurations.fold(
                okHttpClient.newBuilder()
                    .addInterceptor(ClientIdParameterInterceptor(clientId))
            ) { client, configurator ->
                configurator(client)
            }
                .build()
        val sessionlessRetrofit = retrofit.newBuilder()
            .client(sessionlessOkHttpClient)
            .build()

        val sessionOkHttpClient = sessionOkHttpClientConfigurations.fold(
            okHttpClient.newBuilder()
                .addInterceptor(AuthenticationHeaderInterceptor(authorizationHeaderUseCase))
                .authenticator(
                    Authenticator(
                        refreshTokenService = adapter.adapt(sessionlessRetrofit.create(refreshServiceClass.java)),
                        authenticationLocalStorage = authenticationLocalStorage,
                        sessionExpiredEventHandler = sessionExpiredEventHandler,
                        isSessionExpiredException = isSessionExpiredException,
                        setAuthorizationHeader = authorizationHeaderUseCase
                    )
                )
        ) { client, configurator ->
            configurator(client)
        }
            .build()

        val sessionRetrofit = sessionlessRetrofit.newBuilder()
            .client(sessionOkHttpClient)
            .build()

        return OauthRetrofitContainer(sessionRetrofit, sessionlessRetrofit)
    }
}
