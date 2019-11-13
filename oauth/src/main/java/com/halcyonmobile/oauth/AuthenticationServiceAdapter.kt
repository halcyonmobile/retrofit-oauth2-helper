/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
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