package com.halcyonmobile.oauth

import retrofit2.HttpException

val Throwable.causeHttpException: HttpException?
    get() {
        var current: Throwable? = this
        while (current?.cause !== current && current != null && current !is HttpException) {
            current = current.cause
        }

        return current as? HttpException
    }
