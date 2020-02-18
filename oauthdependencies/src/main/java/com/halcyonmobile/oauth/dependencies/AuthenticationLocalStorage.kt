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