/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth.dependencies

import androidx.annotation.WorkerThread

/**
 * Simple storage for the session data.
 *
 * Note: workerThread annotation is just a suggestion
 */
interface AuthenticationLocalStorage {

    /**
     * The id of signed-in user.
     */
    @set:WorkerThread
    @get:WorkerThread
    var userId: String

    /**
     * The access Token of signed-in user.
     */
    @set:WorkerThread
    @get:WorkerThread
    var accessToken: String

    /**
     * The type of [accessToken] of signed-in used.
     */
    @set:WorkerThread
    @get:WorkerThread
    var tokenType: String

    /**
     * The refresh Token of signed-in used.
     */
    @set:WorkerThread
    @get:WorkerThread
    var refreshToken: String

    /**
     * Clears all data from the storage, should be called when the session is no longer valid.
     */
    @WorkerThread
    fun clear()
}