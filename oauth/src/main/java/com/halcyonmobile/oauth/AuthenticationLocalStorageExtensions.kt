/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
 */
package com.halcyonmobile.oauth

import androidx.annotation.WorkerThread
import com.halcyonmobile.oauth.dependencies.AuthenticationLocalStorage

/**
 * Saves all the necessary data from the [sessionDataResponse] to the given [receiver][AuthenticationLocalStorage]
 */
@WorkerThread
fun AuthenticationLocalStorage.save(sessionDataResponse: SessionDataResponse) {
    accessToken = sessionDataResponse.token
    refreshToken = sessionDataResponse.refreshToken
    tokenType = sessionDataResponse.tokenType
    userId = sessionDataResponse.userId
}