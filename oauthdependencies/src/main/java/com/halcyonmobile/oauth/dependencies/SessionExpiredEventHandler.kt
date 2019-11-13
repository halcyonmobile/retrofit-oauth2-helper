/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth.dependencies

import androidx.annotation.WorkerThread

/**
 * Callback to handle SessionExpiration which means the authentication token expired and the refreshToken can't be used to get a new one.
 * The user has to log in again.
 *
 * When this method is called the [AuthenticationLocalStorage] is already cleared.
 */
interface SessionExpiredEventHandler {

    @WorkerThread
    fun onSessionExpired()
}