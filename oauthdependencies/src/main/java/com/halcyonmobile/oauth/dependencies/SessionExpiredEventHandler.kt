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
 * Callback to handle SessionExpiration which means the authentication token expired and the refreshToken can't be used to get a new one.
 * The user has to log in again.
 *
 * When this method is called the [AuthenticationLocalStorage] is already cleared.
 */
interface SessionExpiredEventHandler {

    @WorkerThread
    fun onSessionExpired()
}