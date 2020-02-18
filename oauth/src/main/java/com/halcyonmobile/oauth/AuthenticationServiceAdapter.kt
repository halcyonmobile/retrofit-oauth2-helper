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

/**
 * Adapter which adapts the given [T] type which should be a retrofit service for refresh token,
 * to a [AuthenticationService] which then can be used by the [com.halcyonmobile.oauth.internal.Authenticator]
 *
 * You may generate simple adapters using [com.halcyonmobile.dependencies.RefreshService] on your [T] retrofit service.
 */
interface AuthenticationServiceAdapter<T> {

    /**
     * When the [com.halcyonmobile.oauth.internal.Authenticator] calls the [AuthenticationService] returned by this
     * function should call the given [service]'s method with the refreshToken from the [AuthenticationService.refreshToken].
     */
    fun adapt(service: T): AuthenticationService
}