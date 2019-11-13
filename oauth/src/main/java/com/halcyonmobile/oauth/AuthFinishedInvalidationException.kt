/*
 * Copyright (c) 2019 Halcyon Mobile
 * https://www.halcyonmobile.com
 * All rights reserved.
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