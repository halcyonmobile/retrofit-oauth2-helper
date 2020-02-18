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

import java.io.IOException

/**
 * A specific exception which is only thrown when a request has a header with [key][INVALIDATION_AFTER_REFRESH_HEADER_NAME] and [value][INVALIDATION_AFTER_REFRESH_HEADER_VALUE] pair
 * AND authentication happened, meaning the refresh and access tokens were changed.
 *
 * This is useful when you have to send the refresh or access token in the request.
 * Then you can add the header and after the tokens are refreshed you will receive this exception.
 *
 * example:
 * ```
 * suspend fun logout(@Header(INVALIDATION_AFTER_REFRESH_HEADER_NAME) authExceptionHeader: String = INVALIDATION_AFTER_REFRESH_HEADER_VALUE)
 * ```
 */
val authFinishedInvalidationException = IOException("Authentication was finished, invalidating the request with this exception", null)

inline fun <T> runCatchingCausedByAuthFinishedInvalidation(crossinline request: () -> T, crossinline doOnCatch: () -> T): T =
    try {
        request()
    } catch (ioException: Throwable) {
        if (ioException.findCauseMatching(authFinishedInvalidationException)) {
            doOnCatch()
        } else {
            throw ioException
        }
    }

suspend inline fun <T> runCatchingCausedByAuthFinishedInvalidationSuspend(crossinline request: suspend () -> T, crossinline doOnCatch: suspend () -> T): T =
    try {
        request()
    } catch (ioException: Throwable) {
        if (ioException.findCauseMatching(authFinishedInvalidationException)) {
            doOnCatch()
        } else {
            throw ioException
        }
    }

fun Throwable.findCauseMatching(toFind: Throwable): Boolean {
    val cause = cause
    return when {
        this === toFind -> true
        this == cause -> false
        cause == null -> false
        else -> cause.findCauseMatching(toFind)
    }
}


const val INVALIDATION_AFTER_REFRESH_HEADER_NAME = "INVALIDATION_AFTER_REFRESH_HEADER_NAME"
const val INVALIDATION_AFTER_REFRESH_HEADER_VALUE = "INVALIDATION_AFTER_REFRESH_HEADER_VALUE"